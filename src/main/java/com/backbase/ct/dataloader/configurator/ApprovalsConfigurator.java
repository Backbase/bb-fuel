package com.backbase.ct.dataloader.configurator;

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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.approval.ApprovalIntegrationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.dto.UserContext;
import com.backbase.ct.dataloader.setup.AccessControlSetup;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.rest.spec.common.types.Currency;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private String policyBId;
    private String policyCId;

    public void setupApprovals(String externalServiceAgreementId, String externalLegalEntityId,
        List<String> externalUserIds) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        createApprovalTypes();
        createPolicies();

        if (globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS)) {
            setupAccessControlAndAssignApprovalTypesForPayments(externalServiceAgreementId, externalUserIds, PAYMENTS_FUNCTIONS);
            assignPaymentsPolicies(externalServiceAgreementId, externalLegalEntityId);
        }

        if (globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS)) {
            setupAccessControlAndAssignApprovalTypesForContacts(externalServiceAgreementId, externalUserIds, singletonList(CONTACTS_FUNCTION_NAME));
            assignContactsPolicies(externalServiceAgreementId, externalLegalEntityId);
        }

    }

    private void createApprovalTypes() {
        approvalTypeAId = approvalIntegrationRestClient.createApprovalType(1);
        LOGGER.info("Approval type A [{}] created", approvalTypeAId);

        approvalTypeBId = approvalIntegrationRestClient.createApprovalType(2);
        LOGGER.info("Approval type B [{}] created", approvalTypeBId);

        approvalTypeCId = approvalIntegrationRestClient.createApprovalType(3);
        LOGGER.info("Approval type C [{}] created", approvalTypeCId);
    }

    private void createPolicies() {
        policyZeroId = approvalIntegrationRestClient.createZeroApprovalPolicy();

        policyAId = approvalIntegrationRestClient.createPolicy(
            createPolicyItemDto(approvalTypeAId, 1));

        LOGGER.info("Policy A [{}] created", policyAId);

        policyBId = approvalIntegrationRestClient.createPolicy(
            createPolicyItemDto(approvalTypeAId, 1),
            createPolicyItemDto(approvalTypeBId, 1));

        LOGGER.info("Policy B [{}] created", policyBId);

        policyCId = approvalIntegrationRestClient.createPolicy(
            createPolicyItemDto(approvalTypeAId, 1),
            createPolicyItemDto(approvalTypeBId, 1),
            createPolicyItemDto(approvalTypeCId, 1));

        LOGGER.info("Policy C [{}] created", policyCId);
    }

    private void assignPaymentsPolicies(String externalServiceAgreementId, String externalLegalEntityId) {
        final List<String> PAYMENTS_FUNCTIONS = asList(
            SEPA_CT_FUNCTION_NAME,
            US_DOMESTIC_WIRE_FUNCTION_NAME,
            US_FOREIGN_WIRE_FUNCTION_NAME);

        for (String paymentsFunction : PAYMENTS_FUNCTIONS) {
            approvalIntegrationRestClient.assignPolicy(getPaymentsPolicyAssignments(
                externalServiceAgreementId,
                externalLegalEntityId,
                paymentsFunction));

            LOGGER.info("Zero approval policy [{}] with upper bound [{}],"
                    + "policy A [{}] with upper bound [{}], "
                    + "policy B [{}] with upper bound [{}] and "
                    + "policy C [{}] unbounded assigned to business function [{}]",
                policyZeroId, UPPER_BOUND_HUNDRED.toPlainString(),
                policyAId, UPPER_BOUND_THOUSAND.toPlainString(),
                policyBId, UPPER_BOUND_HUNDRED_THOUSAND.toPlainString(),
                policyCId, paymentsFunction);
        }
    }

    private void assignContactsPolicies(String externalServiceAgreementId, String externalLegalEntityId) {
        approvalIntegrationRestClient
            .assignPolicy(getContactsPolicyAssignments(externalServiceAgreementId, externalLegalEntityId));

        LOGGER.info("Policy A [{}] assigned to business function [{}]", policyAId, CONTACTS_FUNCTION_NAME);
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

        policyBoundMap.put(policyBId, new Currency()
            .withCurrencyCode(currencyCode)
            .withAmount(UPPER_BOUND_HUNDRED_THOUSAND));

        policyBoundMap.put(policyCId, null);

        for (Map.Entry<String, Currency> entry : policyBoundMap.entrySet()) {
            String policyId = entry.getKey();
            Currency upperBound = entry.getValue();

            policyAssignmentRequests.add(createPolicyAssignmentRequest(
                externalServiceAgreementId,
                externalLegalEntityId,
                PAYMENTS_RESOURCE_NAME,
                paymentsFunction,
                createPolicyAssignmentRequestBounds(policyId, upperBound);
        }

        return policyAssignmentRequests;
    }

    private List<IntegrationPolicyAssignmentRequest> getContactsPolicyAssignments(
        String externalServiceAgreementId,
        String externalLegalEntityId) {
        return singletonList(createPolicyAssignmentRequest(
            externalServiceAgreementId,
            externalLegalEntityId,
            CONTACTS_RESOURCE_NAME,
            CONTACTS_FUNCTION_NAME,
            createPolicyAssignmentRequestBounds(policyAId, null)));
    }

    private void setupAccessControlAndAssignApprovalTypesForPayments(
        String externalServiceAgreementId,
        List<String> externalUserIds,
        List<String> functionNames) {

        String internalServiceAgreementId = serviceAgreementsIntegrationRestClient
            .retrieveServiceAgreementByExternalId(externalServiceAgreementId)
            .getId();

        String paymentsFunctionGroupAId = accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            functionNames);

        String paymentsFunctionGroupBId = accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            functionNames);

        String paymentsFunctionGroupCId = accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            functionNames);

        List<String> functionIds = accessGroupIntegrationRestClient
            .retrieveFunctions(functionNames)
            .stream().map(FunctionsGetResponseBody::getFunctionId)
            .collect(toList());

        List<String> existingDataGroupIds = new ArrayList<>();

        for (String externalUserId : externalUserIds) {
            String internalUserId = userPresentationRestClient.getUserByExternalId(externalUserId)
                .getId();

            functionIds.forEach(functionId -> existingDataGroupIds.addAll(
                accessGroupPresentationRestClient.getDataGroupIdsByFunctionId(
                    internalServiceAgreementId,
                    internalUserId,
                    functionId)));
        }

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

        String contactsFunctionGroupAId = accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
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
            createApprovalTypeAssignmentDto(approvalTypeAId, contactsFunctionGroupAId);
    }
}
