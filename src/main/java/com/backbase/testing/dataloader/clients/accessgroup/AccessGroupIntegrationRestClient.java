package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;

public class AccessGroupIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE = "/accessgroup-integration-service/" + SERVICE_VERSION + "/accessgroups";
    private static final String ENDPOINT_CONFIG_FUNCTIONS = ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE + "/config/functions";
    private static final String ENDPOINT_FUNCTION = ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE + "/function";
    private static final String ENDPOINT_USERS_PERMISSIONS = ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE + "/users/permissions";
    private static final String ENDPOINT_DATA = ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE + "/data";

    public AccessGroupIntegrationRestClient() {
        super(globalProperties.get(PROPERTY_ENTITLEMENTS_BASE_URI));
    }

    public Response ingestFunctionGroup(FunctionGroupsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_FUNCTION);
    }

    public Response ingestDataGroup(DataGroupsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_DATA);
    }

    public Response retrieveFunctions() {
        return requestSpec()
                .contentType(ContentType.JSON)
                .get(ENDPOINT_CONFIG_FUNCTIONS);
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
                .post(ENDPOINT_USERS_PERMISSIONS);
    }
}
