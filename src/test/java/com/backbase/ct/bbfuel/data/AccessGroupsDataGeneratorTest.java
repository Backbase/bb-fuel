package com.backbase.ct.bbfuel.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.dbs.accesscontrol.ac_function_group.integration.v1.model.Permission;
import com.backbase.dbs.accesscontrol.ac_permission_set.integration.v1.model.PermissionItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccessGroupsDataGeneratorTest {

    private static final Set<String> PRIVILEGES = Set.of("execute", "view", "create", "edit", "delete", "approve",
        "cCancel");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static List<com.backbase.ct.bbfuel.dto.entitlement.Permission> createPermissions(String businessFunction,
        String... privileges) {
        List<com.backbase.ct.bbfuel.dto.entitlement.Permission> permissions = new ArrayList<>();
        permissions.add(com.backbase.ct.bbfuel.dto.entitlement.Permission.builder()
            .businessFunction(businessFunction).privileges(Arrays.asList(privileges)).build());

        return permissions;
    }

    private static List<PermissionItem> createPermissionItems(String businessFunction) {
        List<PermissionItem> functions = new ArrayList<>();
        functions.add(new PermissionItem()
            .businessFunctionName("awesome business")
            .privileges(PRIVILEGES));
        functions.add(new PermissionItem()
            .businessFunctionName(businessFunction)
            .privileges(PRIVILEGES));

        return functions;
    }

    @Test
    public void testCreatePermissionsForJobProfile() {
        String businessFunction = "Manage Users";
        JobProfile jobProfile = new JobProfile();
        jobProfile.setPermissions(createPermissions(businessFunction, "view", "create", "approve"));

        List<PermissionItem> functions = createPermissionItems(businessFunction);
        List<Permission> permissions = AccessGroupsDataGenerator.createPermissionsForJobProfile(jobProfile, functions);
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getPrivileges(), hasSize(3));
    }

    @Test
    public void testCreateFailsOnInvalidPrivilege() {
        String businessFunction = "Manage Users";
        JobProfile jobProfile = new JobProfile();
        jobProfile.setPermissions(createPermissions(businessFunction, "write"));
        List<PermissionItem> functions = createPermissionItems(businessFunction);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("does not allow for privilege"));
        AccessGroupsDataGenerator.createPermissionsForJobProfile(jobProfile, functions);
    }

    @Test
    public void testCreateFailsOnInvalidBusinessFunction() {
        String businessFunction = "Manage Melons";
        JobProfile jobProfile = new JobProfile();
        jobProfile.setPermissions(createPermissions(businessFunction, "write"));
        List<PermissionItem> functions = createPermissionItems("Manage Users");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("No matching business function"));
        AccessGroupsDataGenerator.createPermissionsForJobProfile(jobProfile, functions);
    }

    @Test
    public void testCreatePermissionsWithAllPrivileges() {
        List<PermissionItem> functions = createPermissionItems("Manage Users");
        List<Permission> permissions = AccessGroupsDataGenerator.createPermissionsWithAllPrivileges(functions);
        assertThat(permissions, hasSize(functions.size()));
        assertThat(permissions.get(0).getPrivileges(), hasSize(functions.get(0).getPrivileges().size()));
    }
}
