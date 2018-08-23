package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermissionsWithAllPrivileges;
import static com.backbase.ct.dataloader.data.ApprovalsDataGenerator.createApprovalTypeAssignmentDto;
import static com.backbase.ct.dataloader.data.ApprovalsDataGenerator.createPolicyAssignmentRequest;
import static com.backbase.ct.dataloader.data.ApprovalsDataGenerator.createPolicyAssignmentRequestBounds;
import static com.backbase.ct.dataloader.data.ApprovalsDataGenerator.createPolicyItemDto;
import static com.backbase.ct.dataloader.data.CommonConstants.CONTACTS_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.CONTACTS_RESOURCE_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.PAYMENTS_RESOURCE_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_CONTACTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS;
import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.util.CommonHelpers.generateRandomNumberInRange;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.approval.ApprovalIntegrationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.rest.spec.common.types.Currency;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalsConfigurator.class);
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ApprovalIntegrationRestClient approvalIntegrationRestClient;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient;
    private final UserPresentationRestClient userPresentationRestClient;

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
        List<String> externalUserIds) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        if (globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS)) {
            setupAccessControlAndAssignApprovalTypesForPayments(externalServiceAgreementId, externalUserIds, singletonList(SEPA_CT_FUNCTION_NAME));
            setupAccessControlAndAssignApprovalTypesForPayments(externalServiceAgreementId, externalUserIds, asList(
                US_DOMESTIC_WIRE_FUNCTION_NAME,
                US_FOREIGN_WIRE_FUNCTION_NAME));
            assignPaymentsPolicies(externalServiceAgreementId, externalLegalEntityId, PAYMENTS_FUNCTIONS, externalUserIds.size());
        }

        if (globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS)) {
            setupAccessControlAndAssignApprovalTypesForContacts(externalServiceAgreementId, externalUserIds, singletonList(CONTACTS_FUNCTION_NAME));
            assignContactsPolicies(externalServiceAgreementId, externalLegalEntityId, singletonList(CONTACTS_FUNCTION_NAME));
        }
    }

    private void createApprovalTypes() {
        approvalTypeAId = approvalIntegrationRestClient.createApprovalType("A", generateRandomNumberInRange(1, 100));
        LOGGER.info("Approval type A [{}] created", approvalTypeAId);

        approvalTypeBId = approvalIntegrationRestClient.createApprovalType("B", generateRandomNumberInRange(100, 200));
        LOGGER.info("Approval type B [{}] created", approvalTypeBId);

        approvalTypeCId = approvalIntegrationRestClient.createApprovalType("C", generateRandomNumberInRange(200, 300));
        LOGGER.info("Approval type C [{}] created", approvalTypeCId);
    }

    private void createPolicies() {
        policyZeroId = approvalIntegrationRestClient.createZeroApprovalPolicy();

        policyAId = approvalIntegrationRestClient.createPolicy(
            createPolicyItemDto(approvalTypeAId, 1));

        LOGGER.info("Policy with approval type A [{}] created", policyAId);

        policyABId = approvalIntegrationRestClient.createPolicy(
            createPolicyItemDto(approvalTypeAId, 1),
            createPolicyItemDto(approvalTypeBId, 1));

        LOGGER.info("Policy with approval types A and B [{}] created", policyABId);

        policyABCId = approvalIntegrationRestClient.createPolicy(
            createPolicyItemDto(approvalTypeAId, 1),
            createPolicyItemDto(approvalTypeBId, 1),
            createPolicyItemDto(approvalTypeCId, 1));

        LOGGER.info("Policy with approval types A, B and C [{}] created", policyABCId);
    }

    private void assignPaymentsPolicies(String externalServiceAgreementId, String externalLegalEntityId, List<String> functionNames, int numberOfUsers) {
        List<IntegrationPolicyAssignmentRequest> listOfAssignments = new ArrayList<>();

        for (String functionName : functionNames) {
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

        LOGGER.info("Policies assigned: {}", listOfAssignments);
    }

    private void assignContactsPolicies(String externalServiceAgreementId, String externalLegalEntityId, List<String> functionNames) {
        List<IntegrationPolicyAssignmentRequest> listOfAssignments = new ArrayList<>();

        for (String functionName : functionNames) {
            List<IntegrationPolicyAssignmentRequest> generatedItems;

            generatedItems = getContactsPolicyAssignments(externalServiceAgreementId, externalLegalEntityId, functionName);

            listOfAssignments.addAll(generatedItems);
        }

        approvalIntegrationRestClient.assignPolicies(listOfAssignments);

        LOGGER.info("Policies assigned: {}", listOfAssignments);
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
                createPolicyAssignmentRequestBounds(policyId, upperBound)));
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
            createPolicyAssignmentRequestBounds(policyZeroId, null)));
    }


    private List<IntegrationPolicyAssignmentRequest> getContactsPolicyAssignments(
        String externalServiceAgreementId,
        String externalLegalEntityId,
        String contactsFunction) {
        return singletonList(createPolicyAssignmentRequest(
            externalServiceAgreementId,
            externalLegalEntityId,
            CONTACTS_RESOURCE_NAME,
            contactsFunction,
            createPolicyAssignmentRequestBounds(policyAId, null)));
    }

    private void setupAccessControlAndAssignApprovalTypesForPayments(
        String externalServiceAgreementId,
        List<String> externalUserIds,
        List<String> functionNames) {
        sort(externalUserIds);

        String internalServiceAgreementId = serviceAgreementsIntegrationRestClient
            .retrieveServiceAgreementByExternalId(externalServiceAgreementId)
            .getId();

        List<FunctionsGetResponseBody> functions = accessGroupIntegrationRestClient
            .retrieveFunctions(functionNames);

        String paymentsFunctionGroupAId = accessGroupIntegrationRestClient.ingestFunctionGroup(externalServiceAgreementId,
            createPermissionsWithAllPrivileges(functions));

        String paymentsFunctionGroupBId = accessGroupIntegrationRestClient.ingestFunctionGroup(externalServiceAgreementId,
            createPermissionsWithAllPrivileges(functions));

        String paymentsFunctionGroupCId = accessGroupIntegrationRestClient.ingestFunctionGroup(externalServiceAgreementId,
            createPermissionsWithAllPrivileges(functions));

        List<String> functionIds = functions
            .stream().map(FunctionsGetResponseBody::getFunctionId)
            .collect(toList());

        Set<String> existingDataGroupIdSet = new HashSet<>();

        for (String externalUserId : externalUserIds) {
            String internalUserId = userPresentationRestClient.getUserByExternalId(externalUserId)
                .getId();

            functionIds.forEach(functionId -> existingDataGroupIdSet.addAll(
                accessGroupPresentationRestClient.getDataGroupIdsByFunctionId(
                    internalServiceAgreementId,
                    internalUserId,
                    functionId)));
        }

        List<String> existingDataGroupIds = new ArrayList<>(existingDataGroupIdSet);

        Map<Integer, List<String>> functionGroupMap = new HashMap<>();

        functionGroupMap.put(0, asList(paymentsFunctionGroupAId, paymentsFunctionGroupBId, paymentsFunctionGroupCId));
        functionGroupMap.put(1, singletonList(paymentsFunctionGroupAId));
        functionGroupMap.put(2, singletonList(paymentsFunctionGroupBId));
        functionGroupMap.put(3, singletonList(paymentsFunctionGroupCId));

        for (int i = 1; i < externalUserIds.size() + 1; i++) {
            List<String> functionGroupIds = functionGroupMap.get(i % 4);

            for (String functionGroupId : functionGroupIds) {
                accessGroupIntegrationRestClient.assignPermissions(
                    externalUserIds.get(i - 1),
                    internalServiceAgreementId,
                    functionGroupId,
                    existingDataGroupIds);
            }
        }

        approvalIntegrationRestClient.assignApprovalTypes(
            createApprovalTypeAssignmentDto(approvalTypeAId, paymentsFunctionGroupAId),
            createApprovalTypeAssignmentDto(approvalTypeBId, paymentsFunctionGroupBId),
            createApprovalTypeAssignmentDto(approvalTypeCId, paymentsFunctionGroupCId));
    }

    private void setupAccessControlAndAssignApprovalTypesForContacts(
        String externalServiceAgreementId,
        List<String> externalUserIds,
        List<String> functionNames) {

        String internalServiceAgreementId = serviceAgreementsIntegrationRestClient
            .retrieveServiceAgreementByExternalId(externalServiceAgreementId)
            .getId();

        String contactsFunctionGroupAId = accessGroupsConfigurator.ingestFunctionGroupWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            functionNames);

        for (String externalUserId : externalUserIds) {
            accessGroupIntegrationRestClient.assignPermissions(
                externalUserId,
                internalServiceAgreementId,
                contactsFunctionGroupAId,
                emptyList());
        }

        approvalIntegrationRestClient.assignApprovalTypes(
            createApprovalTypeAssignmentDto(approvalTypeAId, contactsFunctionGroupAId));
    }
}
