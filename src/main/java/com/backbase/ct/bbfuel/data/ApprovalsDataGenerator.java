package com.backbase.ct.bbfuel.data;

import static java.util.Collections.emptyList;

import com.backbase.dbs.approval.integration.spec.IntegrationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.integration.spec.IntegrationDeletePolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequestBounds;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyItemDto;
import com.backbase.dbs.approval.integration.spec.IntegrationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyAssignmentBulkRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyRequest;
import com.backbase.dbs.approval.spec.PostApprovalTypeRequest;
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

    public static IntegrationPostPolicyRequest createPostPolicyRequest(String policyName,
        List<IntegrationPolicyItemDto> policyItems) {
        return new IntegrationPostPolicyRequest()
            .withName(policyName)
            .withDescription(policyName)
            .withItems(policyItems);
    }

    public static IntegrationPolicyItemDto createPolicyItemDto(String approvalTypeId, int numberOfApprovals) {
        IntegrationPolicyItemDto policyItem = new IntegrationPolicyItemDto();
        policyItem.setApprovalTypeId(approvalTypeId);
        policyItem.setNumberOfApprovals(numberOfApprovals);
        return policyItem;
    }

    public static IntegrationPostPolicyRequest createPostPolicyRequestWithZeroPolicyItems() {
        String name = "0 approvers";

        return new IntegrationPostPolicyRequest()
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
        String externalLegalEntityId,
        String resource,
        String function,
        List<IntegrationPolicyAssignmentRequestBounds> integrationPolicyAssignmentRequestBounds) {
        return new IntegrationPolicyAssignmentRequest()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withExternalLegalEntityId(externalLegalEntityId)
            .withResource(resource)
            .withFunction(function)
            .withBounds(integrationPolicyAssignmentRequestBounds);
    }

    public static IntegrationPolicyAssignmentRequestBounds createPolicyAssignmentRequestBounds(String policyId,
        Currency upperBound) {
        return new IntegrationPolicyAssignmentRequestBounds()
            .withPolicyId(policyId)
            .withUpperBound(upperBound);
    }

    public static IntegrationDeletePolicyAssignmentRequest createDeletePolicyAssignmentRequest(
        String externalServiceAgreementId, String externalLegalEntityId, String resource, String function) {
        return new IntegrationDeletePolicyAssignmentRequest()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withExternalLegalEntityId(externalLegalEntityId)
            .withResource(resource)
            .withFunction(function);
    }
}
