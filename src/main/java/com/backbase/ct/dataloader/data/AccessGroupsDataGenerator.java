package com.backbase.ct.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.IntegrationPrivilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;

public class AccessGroupsDataGenerator {

    private static Faker faker = new Faker();

    public static FunctionGroupPostRequestBody generateFunctionGroupPostRequestBody(String externalServiceAgreementId,
        String functionGroupName, List<Permission> permissions) {
        return new FunctionGroupPostRequestBody()
            .withName(functionGroupName)
            .withDescription(functionGroupName)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withPermissions(permissions);
    }

    public static DataGroupPostRequestBody generateDataGroupPostRequestBody(String externalServiceAgreementId,
        String dataGroupName, String type, List<String> items) {
        return new DataGroupPostRequestBody()
            .withName(dataGroupName)
            .withDescription(dataGroupName)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withType(type)
            .withItems(items);
    }

    public static Permission createPermission(String functionId, List<Privilege> privileges) {
        return new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privileges);
    }


    public static List<Permission> createPermissionsWithAllPrivileges(List<FunctionsGetResponseBody> functions) {
        List<Permission> permissions = new ArrayList<>();

        functions.forEach(function -> permissions.add(createPermissionWithAllPrivileges(function)));

        return permissions;
    }

    private static Permission createPermissionWithAllPrivileges(FunctionsGetResponseBody function) {
        List<IntegrationPrivilege> integrationPrivileges = function.getPrivileges();
        List<Privilege> privileges = new ArrayList<>();

        for (IntegrationPrivilege integrationPrivilege : integrationPrivileges) {
            privileges.add(new Privilege().withPrivilege(integrationPrivilege.getPrivilege()));
        }

        return createPermission(function.getFunctionId(), privileges);
    }
}
