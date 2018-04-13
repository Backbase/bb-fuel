package com.backbase.ct.dataloader.clients.accessgroup;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupIntegrationRestClient extends AbstractRestClient {

    private static final String ENTITLEMENTS = globalProperties.getString(CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_INTEGRATION_SERVICE = "accessgroup-integration-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_CONFIG_FUNCTIONS = ENDPOINT_ACCESS_GROUPS + "/config/functions";
    private static final String ENDPOINT_FUNCTION = ENDPOINT_ACCESS_GROUPS + "/function-groups";
    private static final String ENDPOINT_USERS_PERMISSIONS = ENDPOINT_ACCESS_GROUPS + "/users/permissions";
    private static final String ENDPOINT_DATA = ENDPOINT_ACCESS_GROUPS + "/data-groups";

    public AccessGroupIntegrationRestClient() {
        super(ENTITLEMENTS, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response ingestFunctionGroup(FunctionGroupPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_FUNCTION));
    }

    public Response ingestDataGroup(DataGroupPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_DATA));
    }

    public Response retrieveFunctions() {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(ENDPOINT_CONFIG_FUNCTIONS));
    }

    public List<FunctionsGetResponseBody> retrieveFunctions(List<String> functionNames) {
        FunctionsGetResponseBody[] functions = retrieveFunctions()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionsGetResponseBody[].class);

        return Arrays.stream(functions)
            .filter(function -> functionNames.contains(function.getName()))
            .collect(Collectors.toList());
    }

    public List<FunctionsGetResponseBody> retrieveFunctionsNotContainingProvidedFunctionNames(List<String> functionNames) {
        FunctionsGetResponseBody[] functions = retrieveFunctions()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionsGetResponseBody[].class);

        return Arrays.stream(functions)
            .filter(function -> !functionNames.contains(function.getName()))
            .collect(Collectors.toList());
    }

    public Response assignPermissions(AssignPermissionsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_USERS_PERMISSIONS));
    }

    @Override
    protected String composeInitialPath() {
        return ACCESS_GROUP_INTEGRATION_SERVICE;
    }

}
