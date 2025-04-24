package com.backbase.ct.bbfuel.client.approval;

import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostApprovalTypeRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostBulkApprovalTypesAssignmentRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostLogicalPolicyRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostPolicyAssignmentBulkRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostPolicyRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostPolicyRequestWithZeroPolicyItems;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.approval.integration.api.v2.model.CreatePolicyItemDto;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPostBulkApprovalTypeAssignmentResponse;
import com.backbase.dbs.approval.integration.api.v2.model.IntegrationPostPolicyAssignmentBulkRequest;
import com.backbase.dbs.approval.integration.api.v2.model.PostApprovalTypeRequest;
import com.backbase.dbs.approval.integration.api.v2.model.PostApprovalTypeResponse;
import com.backbase.dbs.approval.integration.api.v2.model.PostPolicyRequest;
import com.backbase.dbs.approval.integration.api.v2.model.PostPolicyResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String APPROVAL_TYPES = "/approval-types";
    private static final String APPROVAL_TYPE_ASSIGNMENTS = "/approval-type-assignments";
    private static final String APPROVAL_TYPE_ASSIGNMENTS_BULK = APPROVAL_TYPE_ASSIGNMENTS + "/bulk";
    private static final String POLICIES = "/policies";
    private static final String POLICY_ASSIGNMENTS = "/policy-assignments";
    private static final String POLICY_ASSIGNMENTS_BULK = POLICY_ASSIGNMENTS + "/bulk";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getApprovals());
        setVersion(SERVICE_VERSION);
    }

    public Response createApprovalType(PostApprovalTypeRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(APPROVAL_TYPES));
    }

    public Response assignApprovalTypes(IntegrationPostBulkApprovalTypeAssignmentRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(APPROVAL_TYPE_ASSIGNMENTS_BULK));
    }

    public Response createPolicy(PostPolicyRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(POLICIES));
    }

    public Response assignPolicies(IntegrationPostPolicyAssignmentBulkRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(POLICY_ASSIGNMENTS_BULK));
    }

    public String createApprovalType(String name, Integer rank) {
        return createApprovalType(createPostApprovalTypeRequest(name, rank))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(PostApprovalTypeResponse.class)
            .getApprovalType()
            .getId();
    }

    public IntegrationPostBulkApprovalTypeAssignmentResponse assignApprovalTypes(
        List<IntegrationApprovalTypeAssignmentDto> approvalTypeAssignmentDtos) {
        return assignApprovalTypes(createPostBulkApprovalTypesAssignmentRequest(approvalTypeAssignmentDtos))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(IntegrationPostBulkApprovalTypeAssignmentResponse.class);
    }

    public String createPolicy(String policyName, List<CreatePolicyItemDto> policyItems) {
        return createPolicy(createPostPolicyRequest(policyName, policyItems))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(PostPolicyResponse.class)
            .getPolicy()
            .getId();
    }

    public String createZeroApprovalPolicy() {
        return createPolicy(createPostPolicyRequestWithZeroPolicyItems())
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(PostPolicyResponse.class)
            .getPolicy()
            .getId();
    }

    public String createPolicyWithLogicalItems(String policyName, List<CreatePolicyItemDto> policyItems) {
        return createPolicy(createPostLogicalPolicyRequest(policyName, policyItems))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(PostPolicyResponse.class)
            .getPolicy()
            .getId();
    }

    public void assignPolicies(List<IntegrationPolicyAssignmentRequest> policyAssignmentRequests) {
        assignPolicies(createPostPolicyAssignmentBulkRequest(policyAssignmentRequests))
            .then()
            .statusCode(SC_NO_CONTENT);
    }
}
