package com.backbase.ct.bbfuel.data;

import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.FunctionGroupItem;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.FunctionGroupItem.TypeEnum;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.FunctionsGetResponseBody;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationDataGroupCreate;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationItemIdentifier;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationPrivilege;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.Permission;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.Privilege;
import java.util.ArrayList;
import java.util.List;

public class AccessGroupsDataGenerator {

    public static FunctionGroupItem generateFunctionGroupPostRequestBody(String externalServiceAgreementId,
        String functionGroupName, String functionGroupType, List<Permission> permissions) {
        return new FunctionGroupItem()
            .name(functionGroupName)
            .description(functionGroupName)
            .type(TypeEnum.fromValue(functionGroupType))
            .externalServiceAgreementId(externalServiceAgreementId)
            .permissions(permissions);
    }

    public static IntegrationDataGroupCreate generateDataGroupPostRequestBody(String externalServiceAgreementId,
        String dataGroupName, String type, List<String> items) {
        List<IntegrationItemIdentifier> dataItems = new ArrayList();
        items.forEach(item -> dataItems.add(new IntegrationItemIdentifier().internalIdIdentifier(item)));

        return new IntegrationDataGroupCreate()
            .name(dataGroupName)
            .description(dataGroupName)
            .externalServiceAgreementId(externalServiceAgreementId)
            .type(type)
            .dataItems(dataItems);
    }

    public static Permission createPermission(String functionId, List<Privilege> privileges) {
        return new Permission()
            .functionId(functionId)
            .assignedPrivileges(privileges);
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
            privileges.add(new Privilege().privilege(integrationPrivilege.getPrivilege()));
        }

        return createPermission(function.getFunctionId(), privileges);
    }

    private static Permission createPermissionForPrivileges(FunctionsGetResponseBody function,
        List<String> privilegeNames, List<String> validNames) {
        List<Privilege> privileges = privilegeNames.stream()
        .map(privilegeName -> {
            if (validNames.contains(privilegeName)) {
                return new Privilege().privilege(privilegeName);
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
