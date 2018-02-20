package com.backbase.testing.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupPostRequestBody;
import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccessGroupsDataGenerator {

    private static Faker faker = new Faker();

    public static FunctionGroupPostRequestBody generateFunctionGroupPostRequestBody(String externalServiceAgreementId, String functionId, List<String> privileges) {
        return new FunctionGroupPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalServiceAgreementId(externalServiceAgreementId)
                .withPermissions(setPermissions(functionId, privileges));
    }

    public static DataGroupPostRequestBody generateDataGroupPostRequestBody(String externalServiceAgreementId, DataGroupPostRequestBody.Type type, List<String> items) {
        return new DataGroupPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalServiceAgreementId(externalServiceAgreementId)
                .withType(type)
                .withItems(items);
    }

    private static List<Permission> setPermissions(String function, List<String> privileges) {
        return Collections.singletonList(createPermission(function, privileges.toArray(new String[privileges.size()])));
    }

    private static Permission createPermission(String function, String... privileges) {
        return new Permission()
                .withFunctionId(function)
                .withAssignedPrivileges(Arrays.stream(privileges)
                        .map(privilege -> new Privilege().withPrivilege(privilege))
                        .collect(Collectors.toList()));
    }
}
