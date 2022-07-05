package com.backbase.ct.bbfuel.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.IntegrationPrivilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccessGroupsDataGeneratorTest {

    private static final String[] PRIVILEGES = "execute,view,create,edit,delete,approve,cCancel".split(",");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
 //test
    private static List<com.backbase.ct.bbfuel.dto.entitlement.Permission> createPermissions(String businessFunction,
        String... privileges) {
        List<com.backbase.ct.bbfuel.dto.entitlement.Permission> permissions = new ArrayList<>();
        permissions.add(com.backbase.ct.bbfuel.dto.entitlement.Permission.builder()
            .businessFunction(businessFunction).privileges(Arrays.asList(privileges)).build());

        return permissions;
    }

    private static List<FunctionsGetResponseBody> createFunctionsGetResponseBodys(String businessFunction) {
        List<FunctionsGetResponseBody> functions = new ArrayList<>();
        functions.add(new FunctionsGetResponseBody()
            .withName("awesome business")
            .withPrivileges(createIntegrationPrivileges()));
        functions.add(new FunctionsGetResponseBody()
            .withName(businessFunction)
            .withPrivileges(createIntegrationPrivileges()));

        return functions;
    }

    private static List<IntegrationPrivilege> createIntegrationPrivileges() {
        List<IntegrationPrivilege> privileges = new ArrayList<>();
        Arrays.stream(PRIVILEGES)
            .forEach(privilege -> {
                privileges.add(new IntegrationPrivilege().withPrivilege(privilege));
            });

        return privileges;
    }

    @Test
    public void testCreatePermissionsForJobProfile() {
        String businessFunction = "Manage Users";
        JobProfile jobProfile = new JobProfile();
        jobProfile.setPermissions(createPermissions(businessFunction, "view", "create", "approve"));

        List<FunctionsGetResponseBody> functions = createFunctionsGetResponseBodys(businessFunction);
        List<Permission> permissions = AccessGroupsDataGenerator.createPermissionsForJobProfile(jobProfile, functions);
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getAssignedPrivileges(), hasSize(3));
    }

    @Test
    public void testCreateFailsOnInvalidPrivilege() {
        String businessFunction = "Manage Users";
        JobProfile jobProfile = new JobProfile();
        jobProfile.setPermissions(createPermissions(businessFunction, "write"));
        List<FunctionsGetResponseBody> functions = createFunctionsGetResponseBodys(businessFunction);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("does not allow for privilege"));
        AccessGroupsDataGenerator.createPermissionsForJobProfile(jobProfile, functions);
    }

    @Test
    public void testCreateFailsOnInvalidBusinessFunction() {
        String businessFunction = "Manage Melons";
        JobProfile jobProfile = new JobProfile();
        jobProfile.setPermissions(createPermissions(businessFunction, "write"));
        List<FunctionsGetResponseBody> functions = createFunctionsGetResponseBodys("Manage Users");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("No matching business function"));
        AccessGroupsDataGenerator.createPermissionsForJobProfile(jobProfile, functions);
    }

    @Test
    public void testCreatePermissionsWithAllPrivileges() {
        List<FunctionsGetResponseBody> functions = createFunctionsGetResponseBodys("Manage Users");
        List<Permission> permissions = AccessGroupsDataGenerator.createPermissionsWithAllPrivileges(functions);
        assertThat(permissions, hasSize(functions.size()));
        assertThat(permissions.get(0).getAssignedPrivileges(), hasSize(functions.get(0).getPrivileges().size()));
    }
}
