package com.backbase.ct.bbfuel.data;

import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.IntegrationItemIdentifier;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.IntegrationDataGroupCreate;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.IntegrationPrivilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import java.util.ArrayList;
import java.util.List;

public class AccessGroupsDataGenerator {

    public static FunctionGroupPostRequestBody generateFunctionGroupPostRequestBody(String externalServiceAgreementId,
        String functionGroupName, List<Permission> permissions) {
        return new FunctionGroupPostRequestBody()
            .withName(functionGroupName)
            .withDescription(functionGroupName)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withPermissions(permissions);
    }

    public static IntegrationDataGroupCreate generateDataGroupPostRequestBody(String externalServiceAgreementId,
        String dataGroupName, String type, List<String> items) {
        List<IntegrationItemIdentifier> dataItems = new ArrayList();
        items.forEach(item -> dataItems.add(new IntegrationItemIdentifier().withInternalIdIdentifier(item)));

        return new IntegrationDataGroupCreate()
            .withName(dataGroupName)
            .withDescription(dataGroupName)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withType(type)
            .withDataItems(dataItems);
    }

    public static Permission createPermission(String functionId, List<Privilege> privileges) {
        return new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privileges);
    }

    private static FunctionsGetResponseBody detectBusinessFunction(String businessFunction,
        List<FunctionsGetResponseBody> functions) {
        return functions
            .stream()
            .filter(functionsGetResponseBody -> functionsGetResponseBody.getName().equals(businessFunction))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No matching business function for " + businessFunction));
    }

    public static List<Permission> createPermissionsForJobProfile(JobProfile jobProfile,
        List<FunctionsGetResponseBody> functions) {
        List<Permission> permissions = new ArrayList<>();

        jobProfile.getPermissions().forEach(permission -> {
            FunctionsGetResponseBody function = detectBusinessFunction(permission.getBusinessFunction(), functions);
            permissions.add(
                createPermissionForPrivileges(function, permission.getPrivileges(),
                    function.getPrivileges()
                        .stream()
                        .map(IntegrationPrivilege::getPrivilege).collect(toList()))
            );
        });
        return permissions;
    }

    public static List<Permission> createPermissionsWithAllPrivileges(List<FunctionsGetResponseBody> functions) {
        return functions.stream()
            .map(AccessGroupsDataGenerator::createPermissionWithAllPrivileges)
            .collect(toList());
    }

    private static Permission createPermissionWithAllPrivileges(FunctionsGetResponseBody function) {
        List<IntegrationPrivilege> integrationPrivileges = function.getPrivileges();
        List<Privilege> privileges = new ArrayList<>();

        for (IntegrationPrivilege integrationPrivilege : integrationPrivileges) {
            privileges.add(new Privilege().withPrivilege(integrationPrivilege.getPrivilege()));
        }

        return createPermission(function.getFunctionId(), privileges);
    }

    private static Permission createPermissionForPrivileges(FunctionsGetResponseBody function,
        List<String> privilegeNames, List<String> validNames) {
        List<Privilege> privileges = privilegeNames.stream()
        .map(privilegeName -> {
            if (validNames.contains(privilegeName)) {
                return new Privilege().withPrivilege(privilegeName);
            } else {
                throw new IllegalArgumentException(
                    String.format("Business Function [%s] does not allow for privilege [%s] but allows: %s",
                        function.getName(), privilegeName,
                        function.getPrivileges().stream()
                            .map(IntegrationPrivilege::getPrivilege)
                            .collect(toList())));
            }
        }).collect(toList());

        return createPermission(function.getFunctionId(), privileges);
    }
}
