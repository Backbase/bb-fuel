package com.backbase.testing.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccessGroupsDataGenerator {

    private Faker faker = new Faker();

    public FunctionGroupsPostRequestBody generateFunctionGroupsPostRequestBody(String externalLegalEntityId, String functionId, List<String> privileges) {
        return new FunctionGroupsPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalLegalEntityId(externalLegalEntityId)
                .withPermissions(setPermissions(functionId, privileges));
    }

    public DataGroupsPostRequestBody generateDataGroupsPostRequestBody(String externalLegalEntityId, DataGroupsPostRequestBody.Type type, List<String> items) {
        return new DataGroupsPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalLegalEntityId(externalLegalEntityId)
                .withType(type)
                .withItems(items);
    }

    private List<Permission> setPermissions(String function, List<String> privileges) {
        return Collections.singletonList(createPermission(function, privileges.toArray(new String[privileges.size()])));
    }

    private Permission createPermission(String function, String... privileges) {
        return new Permission()
                .withFunctionId(function)
                .withAssignedPrivileges(Arrays.stream(privileges)
                        .map(privilege -> new Privilege().withPrivilege(privilege))
                        .collect(Collectors.toList()));
    }
}
