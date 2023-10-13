package com.backbase.ct.bbfuel.client.accessgroup;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.dto.entitlement.AssignablePermissionSet;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.IntegrationDataGroupCreate;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.IntegrationPrivilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.IntegrationAssignUserPermissions;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.IntegrationFunctionGroupDataGroup;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessGroupIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_FUNCTION = ENDPOINT_ACCESS_GROUPS + "/function-groups";
    private static final String ENDPOINT_USERS_PERMISSIONS =
        ENDPOINT_ACCESS_GROUPS + "/users/permissions/user-permissions";
    private static final String ENDPOINT_DATA = ENDPOINT_ACCESS_GROUPS + "/data-groups/batch";
    private static final String ENDPOINT_ASSIGNABLE_PERMISSION_SETS_BY_NAME =
        ENDPOINT_ACCESS_GROUPS + "/permission-sets";
    private static final String REGUlAR_USER_APS_NAME = "Regular user APS";
    private List<FunctionsGetResponseBody> allBusinessFunctions = new ArrayList<>();

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccessgroup());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestFunctionGroup(FunctionGroupPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_FUNCTION));
    }

    public Response ingestDataGroup(IntegrationDataGroupCreate... body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_DATA));
    }

    public List<FunctionsGetResponseBody> retrieveFunctions() {
        if (allBusinessFunctions.isEmpty()) {
            retrieveDefaultUserAps()
                .get(0)
                .getPermissions()
                .forEach(permission -> {
                    allBusinessFunctions.add(
                        new FunctionsGetResponseBody()
                            .withFunctionId(permission.getFunctionId())
                            .withName(permission.getFunctionName())
                            .withResource(permission.getResourceName())
                            .withPrivileges(permission.getPrivileges()
                                .stream()
                                .map(privilege -> new IntegrationPrivilege().withPrivilege(privilege))
                                .collect(Collectors.toList()))
                    );
                });
        }
        return allBusinessFunctions;
    }

    private List<AssignablePermissionSet> retrieveDefaultUserAps() {
        return asList(requestSpec()
            .contentType(ContentType.JSON)
            .queryParam("name", REGUlAR_USER_APS_NAME)
            .get(getPath(ENDPOINT_ASSIGNABLE_PERMISSION_SETS_BY_NAME))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(AssignablePermissionSet[].class));
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

    public Response assignPermissions(IntegrationAssignUserPermissions body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(singletonList(body))
            .put(getPath(ENDPOINT_USERS_PERMISSIONS));
    }

    public Response assignPermissions(
        String externalUserId,
        String externalServiceAgreementId,
        List<IntegrationFunctionGroupDataGroup> functionGroupDataGroups) {

        return assignPermissions(new IntegrationAssignUserPermissions()
            .withExternalUserId(externalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withFunctionGroupDataGroups(functionGroupDataGroups));
    }
}
