package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createApprovalTypeAssignmentDto;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPolicyAssignmentRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPolicyAssignmentRequestBounds;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPolicyItemDto;
import static com.backbase.ct.bbfuel.data.CommonConstants.CONTACTS_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.CONTACTS_RESOURCE_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENTS_RESOURCE_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_CONTACTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.approval.ApprovalIntegrationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.ct.bbfuel.service.JobProfileService;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.rest.spec.common.types.Currency;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalsConfigurator {
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ApprovalIntegrationRestClient approvalIntegrationRestClient;
    private final JobProfileService jobProfileService;

    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
    private static final List<String> PAYMENTS_FUNCTIONS = asList(
        SEPA_CT_FUNCTION_NAME,
        US_DOMESTIC_WIRE_FUNCTION_NAME,
        US_FOREIGN_WIRE_FUNCTION_NAME);
    private static final BigDecimal UPPER_BOUND_HUNDRED = new BigDecimal("100.0");
    private static final BigDecimal UPPER_BOUND_THOUSAND = new BigDecimal("1000.0");
    private static final BigDecimal UPPER_BOUND_HUNDRED_THOUSAND = new BigDecimal("100000.0");
    private String approvalTypeAId;
    private String approvalTypeBId;
    private String approvalTypeCId;
    private String policyZeroId;
    private String policyAId;
    private String policyABId;
    private String policyABCId;

    public void setupApprovalTypesAndPolicies() {
        createApprovalTypes();
        createPolicies();
    }

    public void setupAccessControlAndPerformApprovalAssignments(String externalServiceAgreementId, String externalLegalEntityId,
        int numberOfUsers) {
        loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        boolean isPaymentsApprovalsEnabled = globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS);
        boolean isContactsApprovalsEnabled = globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS);

        if (isPaymentsApprovalsEnabled || isContactsApprovalsEnabled) {
            setupAccessControlAndAssignApprovalTypes(externalServiceAgreementId);
        }
        if (isPaymentsApprovalsEnabled) {
            assignPaymentsPolicies(externalServiceAgreementId, externalLegalEntityId, numberOfUsers);
        }
        if (isContactsApprovalsEnabled) {
            assignContactsPolicies(externalServiceAgreementId, externalLegalEntityId);
        }
    }

    private void createApprovalTypes() {
        approvalTypeAId = approvalIntegrationRestClient.createApprovalType("A", generateRandomNumberInRange(1, 100));
        log.info("Approval type A [{}] created", approvalTypeAId);

        approvalTypeBId = approvalIntegrationRestClient.createApprovalType("B", generateRandomNumberInRange(100, 200));
        log.info("Approval type B [{}] created", approvalTypeBId);

        approvalTypeCId = approvalIntegrationRestClient.createApprovalType("C", generateRandomNumberInRange(200, 300));
        log.info("Approval type C [{}] created", approvalTypeCId);
    }

    private void createPolicies() {
        policyZeroId = approvalIntegrationRestClient.createZeroApprovalPolicy();

        policyAId = approvalIntegrationRestClient.createPolicy(singletonList(
            createPolicyItemDto(approvalTypeAId, 1)));

        log.info("Policy with approval type A [{}] created", policyAId);

        policyABId = approvalIntegrationRestClient.createPolicy(asList(
            createPolicyItemDto(approvalTypeAId, 1),
            createPolicyItemDto(approvalTypeBId, 1)));

        log.info("Policy with approval types A and B [{}] created", policyABId);

        policyABCId = approvalIntegrationRestClient.createPolicy(asList(
            createPolicyItemDto(approvalTypeAId, 1),
            createPolicyItemDto(approvalTypeBId, 1),
            createPolicyItemDto(approvalTypeCId, 1)));

        log.info("Policy with approval types A, B and C [{}] created", policyABCId);
    }

    private void assignPaymentsPolicies(String externalServiceAgreementId, String externalLegalEntityId,
        int numberOfUsers) {
        List<IntegrationPolicyAssignmentRequest> listOfAssignments = new ArrayList<>();

        for (String functionName : PAYMENTS_FUNCTIONS) {
            List<IntegrationPolicyAssignmentRequest> generatedItems;

            if (numberOfUsers < 3) {
                generatedItems = getPaymentsPolicyAssignmentsBasedOnZeroApprovalPolicyOnly(
                    externalServiceAgreementId,
                    externalLegalEntityId,
                    functionName);
            } else {
                generatedItems = getPaymentsPolicyAssignments(
                    externalServiceAgreementId,
                    externalLegalEntityId,
                    functionName);
            }

            listOfAssignments.addAll(generatedItems);
        }

        approvalIntegrationRestClient.assignPolicies(listOfAssignments);

        log.info("Policies assigned: {}", listOfAssignments);
    }

    private void assignContactsPolicies(String externalServiceAgreementId, String externalLegalEntityId) {
        List<IntegrationPolicyAssignmentRequest> listOfAssignments = getContactsPolicyAssignments(
            externalServiceAgreementId, externalLegalEntityId);

        approvalIntegrationRestClient.assignPolicies(listOfAssignments);

        log.info("Policies assigned: {}", listOfAssignments);
    }

    private List<IntegrationPolicyAssignmentRequest> getPaymentsPolicyAssignments(
        String externalServiceAgreementId,
        String externalLegalEntityId,
        String paymentsFunction) {
        String currencyCode = SEPA_CT_FUNCTION_NAME.equals(paymentsFunction) ? "EUR" : "USD";

        List<IntegrationPolicyAssignmentRequest> policyAssignmentRequests = new ArrayList<>();
        Map<String, Currency> policyBoundMap = new HashMap<>();

        policyBoundMap.put(policyZeroId, new Currency()
            .withCurrencyCode(currencyCode)
            .withAmount(UPPER_BOUND_HUNDRED));

        policyBoundMap.put(policyAId, new Currency()
            .withCurrencyCode(currencyCode)
            .withAmount(UPPER_BOUND_THOUSAND));

        policyBoundMap.put(policyABId, new Currency()
            .withCurrencyCode(currencyCode)
            .withAmount(UPPER_BOUND_HUNDRED_THOUSAND));

        policyBoundMap.put(policyABCId, null);

        for (Map.Entry<String, Currency> entry : policyBoundMap.entrySet()) {
            String policyId = entry.getKey();
            Currency upperBound = entry.getValue();

            policyAssignmentRequests.add(createPolicyAssignmentRequest(
                externalServiceAgreementId,
                externalLegalEntityId,
                PAYMENTS_RESOURCE_NAME,
                paymentsFunction,
                singletonList(createPolicyAssignmentRequestBounds(policyId, upperBound))));
        }

        return policyAssignmentRequests;
    }

    private List<IntegrationPolicyAssignmentRequest> getPaymentsPolicyAssignmentsBasedOnZeroApprovalPolicyOnly(
        String externalServiceAgreementId,
        String externalLegalEntityId,
        String paymentsFunction) {
        return singletonList(createPolicyAssignmentRequest(
            externalServiceAgreementId,
            externalLegalEntityId,
            PAYMENTS_RESOURCE_NAME,
            paymentsFunction,
            singletonList(createPolicyAssignmentRequestBounds(policyZeroId, null))));
    }

    private List<IntegrationPolicyAssignmentRequest> getContactsPolicyAssignments(
        String externalServiceAgreementId,
        String externalLegalEntityId) {
        return singletonList(createPolicyAssignmentRequest(
            externalServiceAgreementId,
            externalLegalEntityId,
            CONTACTS_RESOURCE_NAME,
            CONTACTS_FUNCTION_NAME,
            singletonList(createPolicyAssignmentRequestBounds(policyAId, null))));
    }

    /**
     * Retrieve assigned job profiles and when they have the expected approval levels A, B and C assign these.
     */
    private void setupAccessControlAndAssignApprovalTypes(String externalServiceAgreementId) {
        List<JobProfile> jobProfiles = jobProfileService.getAssignedJobProfiles(externalServiceAgreementId);
        if (jobProfiles == null) {
            throw new IllegalStateException("Job profiles are missing for external service agreement "
                + externalServiceAgreementId);
        }
        List<String> referencedApprovalLevels = jobProfiles.stream()
            .map(JobProfile::getApprovalLevel)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!referencedApprovalLevels.containsAll(asList("A", "B", "C"))) {
            log.info("No approval type assignments needed as the agreement belongs to retail {}",
                referencedApprovalLevels);
            return;
        }

        String functionGroupAId = jobProfileService.findByApprovalLevelAndExternalServiceAgreementId(
            "A", externalServiceAgreementId).getId();
        String functionGroupBId = jobProfileService.findByApprovalLevelAndExternalServiceAgreementId(
            "B", externalServiceAgreementId).getId();
        String functionGroupCId = jobProfileService.findByApprovalLevelAndExternalServiceAgreementId(
            "C", externalServiceAgreementId).getId();

        approvalIntegrationRestClient.assignApprovalTypes(asList(
            createApprovalTypeAssignmentDto(approvalTypeAId, functionGroupAId),
            createApprovalTypeAssignmentDto(approvalTypeBId, functionGroupBId),
            createApprovalTypeAssignmentDto(approvalTypeCId, functionGroupCId)));
    }
}
