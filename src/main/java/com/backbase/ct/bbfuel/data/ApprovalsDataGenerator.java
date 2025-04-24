package com.backbase.ct.bbfuel.data;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.backbase.dbs.approval.integration.api.v2.model.CreatePolicyItemDto;
import com.backbase.dbs.approval.integration.api.v2.model.Currency;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPolicyAssignmentRequestBounds;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPostPolicyAssignmentBulkRequest;
import com.backbase.dbs.approval.integration.api.v2.model.LogicalOperator;
import com.backbase.dbs.approval.integration.api.v2.model.LogicalPolicyItemDto;
import com.backbase.dbs.approval.integration.api.v2.model.PostApprovalTypeRequest;
import com.backbase.dbs.approval.integration.api.v2.model.PostPolicyRequest;
import java.util.List;

public class ApprovalsDataGenerator {

    public static PostApprovalTypeRequest createPostApprovalTypeRequest(String name, Integer rank) {
        return new PostApprovalTypeRequest()
            .withName(name)
            .withDescription(name)
            .withRank(rank);
    }

    public static IntegrationPostBulkApprovalTypeAssignmentRequest createPostBulkApprovalTypesAssignmentRequest(
        List<IntegrationApprovalTypeAssignmentDto> approvalTypeAssignmentDtos) {
        return new IntegrationPostBulkApprovalTypeAssignmentRequest()
            .withApprovalTypeAssignments(approvalTypeAssignmentDtos);
    }

    public static IntegrationApprovalTypeAssignmentDto createApprovalTypeAssignmentDto(String approvalTypeId,
        String jobProfileId) {
        return new IntegrationApprovalTypeAssignmentDto()
            .withApprovalTypeId(approvalTypeId)
            .withJobProfileId(jobProfileId);
    }

    public static PostPolicyRequest createPostPolicyRequest(String policyName,
        List<CreatePolicyItemDto> policyItems) {
        return new PostPolicyRequest()
            .withName(policyName)
            .withDescription(policyName)
            .withItems(policyItems);
    }

    public static CreatePolicyItemDto createPolicyItemDto(String approvalTypeId, int numberOfApprovals) {
        CreatePolicyItemDto policyItem = new CreatePolicyItemDto();
        policyItem.setApprovalTypeId(approvalTypeId);
        policyItem.setNumberOfApprovals(numberOfApprovals);
        return policyItem;
    }

    public static LogicalPolicyItemDto createLogicalPolicyItemDto(List<CreatePolicyItemDto> policyItems) {
        LogicalPolicyItemDto policyItem = new LogicalPolicyItemDto();
        policyItem.setRank(1);
        policyItem.setOperator(LogicalOperator.OR);
        policyItem.setItems(policyItems);
        return policyItem;
    }

    public static PostPolicyRequest createPostPolicyRequestWithZeroPolicyItems() {
        String name = "0 approvers";

        return new PostPolicyRequest()
            .withName(name)
            .withDescription(name)
            .withItems(emptyList());
    }

    public static PostPolicyRequest createPostLogicalPolicyRequest(String policyName,
        List<CreatePolicyItemDto> policyItems) {
        return new PostPolicyRequest()
            .withName(policyName)
            .withDescription(policyName)
            .withLogicalItems(singletonList(createLogicalPolicyItemDto(policyItems)));
    }

    public static IntegrationPostPolicyAssignmentBulkRequest createPostPolicyAssignmentBulkRequest(
        List<IntegrationPolicyAssignmentRequest> policyAssignmentRequests) {
        return new IntegrationPostPolicyAssignmentBulkRequest()
            .withPolicyAssignments(policyAssignmentRequests);
    }

    public static IntegrationPolicyAssignmentRequest createPolicyAssignmentRequest(String externalServiceAgreementId,
        String resource,
        String function,
        List<IntegrationPolicyAssignmentRequestBounds> integrationPolicyAssignmentRequestBounds) {
        return new IntegrationPolicyAssignmentRequest()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResource(resource)
            .withFunctions(singletonList(function))
            .withBounds(integrationPolicyAssignmentRequestBounds);
    }

    public static IntegrationPolicyAssignmentRequestBounds createPolicyAssignmentRequestBounds(String policyId,
        Currency upperBound) {
        return new IntegrationPolicyAssignmentRequestBounds()
            .withPolicyId(policyId)
            .withUpperBound(upperBound);
    }
}
