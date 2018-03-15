package com.backbase.testing.dataloader.clients.accessgroup;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LOCAL_ENTITLEMENTS_BASE_URI;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class AccessGroupIntegrationRestClient extends AbstractRestClient {

    private static final String ENTITLEMENTS = globalProperties.getString(PROPERTY_ENTITLEMENTS_BASE_URI);
    private static final String LOCAL_ENTITLEMENTS = globalProperties.getString(PROPERTY_LOCAL_ENTITLEMENTS_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_INTEGRATION_SERVICE = "accessgroup-integration-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_CONFIG_FUNCTIONS = ENDPOINT_ACCESS_GROUPS + "/config/functions";
    private static final String ENDPOINT_FUNCTION = ENDPOINT_ACCESS_GROUPS + "/function-groups";
    private static final String ENDPOINT_USERS_PERMISSIONS = ENDPOINT_ACCESS_GROUPS + "/users/permissions";
    private static final String ENDPOINT_DATA = ENDPOINT_ACCESS_GROUPS + "/data-groups";

    public AccessGroupIntegrationRestClient() {
        super(USE_LOCAL ? LOCAL_ENTITLEMENTS : ENTITLEMENTS, SERVICE_VERSION);
        setInitialPath(ACCESS_GROUP_INTEGRATION_SERVICE);
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

    public FunctionsGetResponseBody retrieveFunctionByName(String functionName) {
        FunctionsGetResponseBody[] allFunctions = retrieveFunctions()
            .thenReturn()
            .getBody()
            .as(FunctionsGetResponseBody[].class);

        for (FunctionsGetResponseBody function : allFunctions) {
            if (function.getName().equals(functionName)) {
                return function;
            }
        }
        return null;
    }

    public Response assignPermissions(AssignPermissionsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_USERS_PERMISSIONS));
    }
}
