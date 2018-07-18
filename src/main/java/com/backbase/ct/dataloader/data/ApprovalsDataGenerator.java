package com.backbase.ct.dataloader.data;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

import com.backbase.dbs.approval.integration.spec.IntegrationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.integration.spec.IntegrationDeletePolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequestBounds;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyItemDto;
import com.backbase.dbs.approval.integration.spec.IntegrationPostApprovalTypeRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyAssignmentBulkRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyRequest;
import com.backbase.rest.spec.common.types.Currency;
import java.util.List;
import java.util.Random;

public class ApprovalsDataGenerator {

    private static Random random = new Random();

    public static IntegrationPostApprovalTypeRequest createPostApprovalTypeRequest(String name, Integer rank) {
        return new IntegrationPostApprovalTypeRequest()
            .withName(name)
            .withDescription(randomAlphabetic(15))
            .withRank(rank);
    }

    public static IntegrationPostBulkApprovalTypeAssignmentRequest createPostBulkApprovalTypesAssignmentRequest(
        IntegrationApprovalTypeAssignmentDto... approvalTypeAssignmentDtos) {
        return new IntegrationPostBulkApprovalTypeAssignmentRequest()
            .withApprovalTypeAssignments(asList(approvalTypeAssignmentDtos));
    }

    public static IntegrationApprovalTypeAssignmentDto createApprovalTypeAssignmentDto(String approvalTypeId,
        String jobProfileId) {
        return new IntegrationApprovalTypeAssignmentDto()
            .withApprovalTypeId(approvalTypeId)
            .withJobProfileId(jobProfileId);
    }

    public static IntegrationPostPolicyRequest createPostPolicyRequest(IntegrationPolicyItemDto... policyItems) {
        return new IntegrationPostPolicyRequest()
            .withName(randomAlphabetic(15))
            .withDescription(randomAlphabetic(15))
            .withAllowSelf(random.nextBoolean())
            .withItems(asList(policyItems));
    }

    public static IntegrationPostPolicyRequest createPostPolicyRequest(boolean allowSelf,
        IntegrationPolicyItemDto... policyItems) {
        return new IntegrationPostPolicyRequest()
            .withName(randomAlphabetic(15))
            .withDescription(randomAlphabetic(15))
            .withAllowSelf(allowSelf)
            .withItems(asList(policyItems));
    }

    public static IntegrationPolicyItemDto createPolicyItemDto(String approvalTypeId, int numberOfApprovals) {
        return new IntegrationPolicyItemDto()
            .withApprovalTypeId(approvalTypeId)
            .withNumberOfApprovals(numberOfApprovals);
    }

    public static IntegrationPostPolicyRequest createPostPolicyRequestWithZeroPolicyItems() {
        return new IntegrationPostPolicyRequest()
            .withName(randomAlphabetic(15))
            .withDescription(randomAlphabetic(15))
            .withAllowSelf(random.nextBoolean())
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
        IntegrationPolicyAssignmentRequestBounds... integrationPolicyAssignmentRequestBounds) {
        return new IntegrationPolicyAssignmentRequest()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withExternalLegalEntityId(externalLegalEntityId)
            .withResource(resource)
            .withFunction(function)
            .withBounds(asList(integrationPolicyAssignmentRequestBounds));
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
