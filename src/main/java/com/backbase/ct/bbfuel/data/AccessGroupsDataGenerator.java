package com.backbase.ct.bbfuel.data;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.dbs.accesscontrol.ac_data_group.integration.v1.model.DataGroupBatchIngest;
import com.backbase.dbs.accesscontrol.ac_function_group.integration.v1.model.FunctionGroupIngest;
import com.backbase.dbs.accesscontrol.ac_function_group.integration.v1.model.FunctionGroupIngest.TypeEnum;
import com.backbase.dbs.accesscontrol.ac_function_group.integration.v1.model.Permission;
import com.backbase.dbs.accesscontrol.ac_permission_set.integration.v1.model.PermissionItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessGroupsDataGenerator {

    public static FunctionGroupIngest generateFunctionGroupPostRequestBody(String externalServiceAgreementId,
        String functionGroupName, String functionGroupType, List<Permission> permissions) {
        return new FunctionGroupIngest()
            .name(functionGroupName)
            .description(functionGroupName)
            .type(getType(functionGroupType))
            .externalServiceAgreementId(externalServiceAgreementId)
            .permissions(permissions);
    }

    private static TypeEnum getType(String functionGroupType) {
        return switch (functionGroupType) {
            case "REGULAR", "CUSTOM" -> TypeEnum.CUSTOM;
            case "TEMPLATE", "REFERENCE" -> TypeEnum.REFERENCE;
            default -> throw new IllegalArgumentException("Unknown function group type: " + functionGroupType);
        };
    }

    public static List<DataGroupBatchIngest> generateDataGroupPostRequestBody(String externalServiceAgreementId,
        String dataGroupName, String type, Set<String> items) {
        return List.of(new DataGroupBatchIngest().name(dataGroupName)
            .description(dataGroupName)
            .externalServiceAgreementId(externalServiceAgreementId)
            .type(type)
            .dataItems(items));
    }

    private static PermissionItem detectBusinessFunction(String businessFunction,
        List<PermissionItem> functions) {
        return functions
            .stream()
            .filter(
                functionsGetResponseBody -> functionsGetResponseBody.getBusinessFunctionName().equals(businessFunction))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No matching business function for " + businessFunction));
    }

    public static List<Permission> createPermissionsForJobProfile(JobProfile jobProfile,
        List<PermissionItem> functions) {
        List<Permission> permissions = new ArrayList<>();

        jobProfile.getPermissions().forEach(permission -> {
            PermissionItem function = detectBusinessFunction(permission.getBusinessFunction(), functions);
            permissions.add(
                createPermissionForPrivileges(function, permission.getPrivileges(),
                    function.getPrivileges())
            );
        });
        return permissions;
    }

    public static List<Permission> createPermissionsWithAllPrivileges(List<PermissionItem> functions) {
        return functions.stream()
            .map(AccessGroupsDataGenerator::createPermissionWithAllPrivileges)
            .toList();
    }

    private static Permission createPermissionWithAllPrivileges(PermissionItem function) {
        return new Permission().businessFunctionName(function.getBusinessFunctionName())
            .resourceName(function.getResourceName())
            .privileges(function.getPrivileges());
    }

    private static Permission createPermissionForPrivileges(PermissionItem function,
        List<String> privilegeNames, Set<String> validNames) {
        Set<String> privileges = privilegeNames.stream()
            .map(privilegeName -> {
                if (validNames.contains(privilegeName)) {
                    return privilegeName;
                } else {
                    throw new IllegalArgumentException(
                        String.format("Business Function [%s] does not allow for privilege [%s] but allows: %s",
                            function.getBusinessFunctionName(), privilegeName, function.getPrivileges()));
                }
            }).collect(Collectors.toSet());

        return new Permission().businessFunctionName(function.getBusinessFunctionName())
            .resourceName(function.getResourceName())
            .privileges(privileges);
    }
}
