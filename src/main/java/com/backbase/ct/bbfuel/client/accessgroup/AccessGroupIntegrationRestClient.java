package com.backbase.ct.bbfuel.client.accessgroup;

import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.AssignUserPermissionsBatch;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.UserPermissionItem;
import com.backbase.dbs.accesscontrol.ac_data_group.integration.v1.model.DataGroupBatchIngest;
import com.backbase.dbs.accesscontrol.ac_function_group.integration.v1.model.FunctionGroupIngest;
import com.backbase.dbs.accesscontrol.ac_permission_set.integration.v1.model.AssignablePermissionSetsList;
import com.backbase.dbs.accesscontrol.ac_permission_set.integration.v1.model.PermissionItem;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessGroupIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v1/access-control";
    private static final String ENDPOINT_FUNCTION = "/function-groups";
    private static final String ENDPOINT_USERS_PERMISSIONS = "/user-permissions";
    private static final String ENDPOINT_DATA = "/data-groups/batch/ingest";
    private static final String ENDPOINT_ASSIGNABLE_PERMISSION_SETS_BY_NAME = "/permission-sets";
    private static final String REGUlAR_USER_APS_NAME = "Regular user APS";
    private List<PermissionItem> allBusinessFunctions = new ArrayList<>();

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccessgroup());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestFunctionGroup(FunctionGroupIngest body) {
        return requestSpec().contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_FUNCTION));
    }

    public Response ingestDataGroup(List<DataGroupBatchIngest> body) {
        return requestSpec().contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_DATA));
    }

    public List<PermissionItem> retrieveFunctions() {
        if (allBusinessFunctions.isEmpty()) {
            allBusinessFunctions.addAll(retrieveDefaultUserAps().getPermissionSets().get(0).getPermissions());
        }
        return allBusinessFunctions;
    }

    public List<PermissionItem> retrieveFunctions(List<String> functionNames) {
        return retrieveFunctions().stream()
            .filter(function -> functionNames.contains(function.getBusinessFunctionName()))
            .collect(Collectors.toList());
    }

    private AssignablePermissionSetsList retrieveDefaultUserAps() {
        return requestSpec()
            .contentType(ContentType.JSON)
            .queryParam("name", REGUlAR_USER_APS_NAME)
            .get(getPath(ENDPOINT_ASSIGNABLE_PERMISSION_SETS_BY_NAME))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(AssignablePermissionSetsList.class);
    }

    public Response assignPermissions(AssignUserPermissionsBatch body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(singletonList(body))
            .put(getPath(ENDPOINT_USERS_PERMISSIONS));
    }

    public Response assignPermissions(
        String externalUserId,
        String externalServiceAgreementId,
        List<UserPermissionItem> functionGroupDataGroups) {

        return assignPermissions(new AssignUserPermissionsBatch()
            .externalUserId(externalUserId)
            .externalServiceAgreementId(externalServiceAgreementId)
            .permissions(functionGroupDataGroups));
    }
}
