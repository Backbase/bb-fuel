package com.backbase.ct.bbfuel.client.accessgroup;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.dto.entitlement.AssignablePermissionSet;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.FunctionGroupItem;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.FunctionsGetResponseBody;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationAssignUserPermissions;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationDataGroupCreate;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationFunctionGroupDataGroup;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationPrivilege;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

@Component
@RequiredArgsConstructor
public class AccessGroupIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_FUNCTION = "/function-groups";
    private static final String ENDPOINT_USERS_PERMISSIONS = "/users/permissions/user-permissions";
    private static final String ENDPOINT_DATA = "/data-groups/batch";
    private static final String ENDPOINT_ASSIGNABLE_PERMISSION_SETS_BY_NAME = "/permission-sets";
    private static final String REGUlAR_USER_APS_NAME = "Regular user APS";
    private List<FunctionsGetResponseBody> allBusinessFunctions = new ArrayList<>();

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccessgroup());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestFunctionGroup(FunctionGroupItem body) {
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
                            .functionId(permission.getFunctionId())
                            .name(permission.getFunctionName())
                            .resource(permission.getResourceName())
                            .privileges(permission.getPrivileges()
                                .stream()
                                .map(privilege -> new IntegrationPrivilege().privilege(privilege))
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
            .externalUserId(externalUserId)
            .externalServiceAgreementId(externalServiceAgreementId)
            .functionGroupDataGroups(functionGroupDataGroups));
    }
}
