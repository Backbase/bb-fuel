package com.backbase.ct.bbfuel.data;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.backbase.dbs.approval.integration.spec.IntegrationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequestBounds;
import com.backbase.dbs.approval.integration.spec.IntegrationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyAssignmentBulkRequest;
import com.backbase.dbs.approval.spec.CreatePolicyItemDto;
import com.backbase.dbs.approval.spec.PostApprovalTypeRequest;
import com.backbase.dbs.approval.spec.PostPolicyRequest;
import com.backbase.rest.spec.common.types.Currency;
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

    public static PostPolicyRequest createPostPolicyRequestWithZeroPolicyItems() {
        String name = "0 approvers";

        return new PostPolicyRequest()
            .withName(name)
            .withDescription(name)
            .withItems(emptyList());
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
