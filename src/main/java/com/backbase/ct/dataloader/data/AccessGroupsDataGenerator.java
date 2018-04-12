package com.backbase.ct.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class AccessGroupsDataGenerator {

    private static Faker faker = new Faker();

    public static FunctionGroupPostRequestBody generateFunctionGroupPostRequestBody(String externalServiceAgreementId, List<Permission> permissions) {
        return new FunctionGroupPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalServiceAgreementId(externalServiceAgreementId)
                .withPermissions(permissions);
    }

    public static DataGroupPostRequestBody generateDataGroupPostRequestBody(String externalServiceAgreementId, String type, List<String> items) {
        return new DataGroupPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
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
        return createPermission(function.getFunctionId(), function.getPrivileges());
    }
}
