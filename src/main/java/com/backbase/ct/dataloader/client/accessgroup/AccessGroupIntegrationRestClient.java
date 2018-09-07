package com.backbase.ct.dataloader.client.accessgroup;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.client.common.AbstractRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccessGroupIntegrationRestClient extends AbstractRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupIntegrationRestClient.class);

    private static final String ENTITLEMENTS = globalProperties
        .getString(CommonConstants.PROPERTY_ACCESS_CONTROL_BASE_URI);
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

    public List<FunctionsGetResponseBody> retrieveFunctions() {
        return asList(requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(ENDPOINT_CONFIG_FUNCTIONS))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionsGetResponseBody[].class));
    }

    public List<FunctionsGetResponseBody> retrieveFunctions(List<String> functionNames) {
        List<FunctionsGetResponseBody> functions = retrieveFunctions();

        return functions.stream()
            .filter(function -> functionNames.contains(function.getName()))
            .collect(Collectors.toList());
    }

    public List<FunctionsGetResponseBody> retrieveFunctionsNotContainingProvidedFunctionNames(
        List<String> functionNames) {
        List<FunctionsGetResponseBody> functions = retrieveFunctions();

        return functions.stream()
            .filter(function -> !functionNames.contains(function.getName()))
            .collect(Collectors.toList());
    }

    public Response assignPermissions(AssignPermissionsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_USERS_PERMISSIONS));
    }

    public void assignPermissions(
        String externalUserId,
        String internalServiceAgreementId,
        String functionGroupId,
        List<String> dataGroupIds) {
        assignPermissions(new AssignPermissionsPostRequestBody()
            .withExternalLegalEntityId(null)
            .withExternalUserId(externalUserId)
            .withServiceAgreementId(internalServiceAgreementId)
            .withFunctionGroupId(functionGroupId)
            .withDataGroupIds(dataGroupIds))
            .then()
            .statusCode(SC_OK);
    }

    @Override
    protected String composeInitialPath() {
        return ACCESS_GROUP_INTEGRATION_SERVICE;
    }

}
