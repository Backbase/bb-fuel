package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.PROFILE_ROLE_ADMIN;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.github.javafaker.Faker;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JobProfileServiceTest {

    private static final String PROFILE_ROLE_EMPLOYEE = "employee";
    private static final String PROFILE_ROLE_MANAGER = "manager";
    private static final String PROFILE_ROLE_RETAIL = "retail";
    private static final String APPROVAL_LEVEL_A = "A";
    private static final String APPROVAL_LEVEL_B = "B";
    private static final String APPROVAL_LEVEL_C = "C";
    private static final List<String> APPROVAL_LEVELS = asList(APPROVAL_LEVEL_A, APPROVAL_LEVEL_B, APPROVAL_LEVEL_C);
    private static final Faker FAKER = new Faker();
    private static final String EXTERNAL_SERVICE_AGREEMENT_ID_1 = FAKER.numerify("EXT_SA_######");
    private static final String EXTERNAL_SERVICE_AGREEMENT_ID_2 = FAKER.numerify("EXT_SA_######");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JobProfileService subject = new JobProfileService();

    @Test
    public void testFindByApprovalLevel() {
        List<String> saIds = asList(EXTERNAL_SERVICE_AGREEMENT_ID_1, EXTERNAL_SERVICE_AGREEMENT_ID_2);
        saIds.forEach(saId -> {
            APPROVAL_LEVELS.forEach(approvalLevel -> {
                JobProfile profile = JobProfile.builder().approvalLevel(APPROVAL_LEVEL_A).build();
                profile.setExternalServiceAgreementId(saId);
                this.subject.saveAssignedProfile(profile);
            });
        });
        JobProfile found = this.subject.findByApprovalLevelAndExternalServiceAgreementId(
            APPROVAL_LEVEL_A, EXTERNAL_SERVICE_AGREEMENT_ID_1);
        assertThat(found.getApprovalLevel(), is(APPROVAL_LEVEL_A));
        assertThat(found.getExternalServiceAgreementId(), is(EXTERNAL_SERVICE_AGREEMENT_ID_1));

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(containsString("No job profile found "));
        this.subject.findByApprovalLevelAndExternalServiceAgreementId(
            "D", EXTERNAL_SERVICE_AGREEMENT_ID_1);
    }

    private static JobProfile createJobProfile(
        String name, String approvalLevel, boolean isRetailProfile, List<String> roles) {
        return JobProfile.builder()
            .jobProfileName(name)
            .approvalLevel(approvalLevel)
            .isRetail(isRetailProfile)
            .roles(roles).build();
    }

    private static JobProfile createAdminProfile() {
        return createJobProfile("Admin", APPROVAL_LEVEL_C, false,
            asList(PROFILE_ROLE_ADMIN, PROFILE_ROLE_MANAGER));
    }

    private static JobProfile createManagerProfile() {
        return createJobProfile("Manager", APPROVAL_LEVEL_B, false,
            asList(PROFILE_ROLE_ADMIN, PROFILE_ROLE_MANAGER));
    }

    private static JobProfile createFinanceEmployeeProfile() {
        return createJobProfile("Finance employee", APPROVAL_LEVEL_A, false,
            asList(PROFILE_ROLE_ADMIN, PROFILE_ROLE_EMPLOYEE));
    }

    private static JobProfile createRetailUserProfile() {
        return createJobProfile("Retail User", null,true,
            asList(PROFILE_ROLE_ADMIN, PROFILE_ROLE_RETAIL));
    }

    private void verifyBusinessProfiles(String role, boolean isRetail, boolean[] expectedValues) {
        String reasonMessage = (isRetail ? "Retail user with " : "Business user with ")
                + role + " role should %s profile with approval level %s";
        int counter = 0;
        JobProfile profile = createFinanceEmployeeProfile();
        boolean isAssignable = this.subject.isJobProfileForUserRole(profile, role, isRetail);
        assertThat(String.format(reasonMessage, (expectedValues[counter] ? "have" : "not have"),
            profile.getApprovalLevel()), isAssignable, is(expectedValues[counter++]));

        profile = createManagerProfile();
        isAssignable = this.subject.isJobProfileForUserRole(profile, role, isRetail);
        assertThat(String.format(reasonMessage, (expectedValues[counter] ? "have" : "not have"),
            profile.getApprovalLevel()), isAssignable, is(expectedValues[counter++]));

        // admin profile is shared for both retail and business
        profile = createAdminProfile();
        isAssignable = this.subject.isJobProfileForUserRole(profile, role, isRetail);
        assertThat(String.format(reasonMessage, (expectedValues[counter] ? "have" : "not have"),
            profile.getApprovalLevel()), isAssignable, is(expectedValues[counter]));
    }

    private void verifyRetailProfile(String role, boolean isRetail, boolean expectedValue) {
        String reasonMessage = (isRetail ? "Retail user with " : "Business user with ")
            + role + " role should %s retail profile";
        JobProfile profile = createRetailUserProfile();
        boolean isAssignable = this.subject.isJobProfileForUserRole(profile, role, isRetail);
        assertThat(String.format(reasonMessage, (expectedValue ? "have" : "not have")),
            isAssignable, is(expectedValue));
    }

    @Test
    public void testBusinessManagerRole() {
        verifyBusinessProfiles(PROFILE_ROLE_MANAGER, false, new boolean[] {false, true, true});
        verifyRetailProfile(PROFILE_ROLE_MANAGER, false, false);
    }

    @Test
    public void testBusinessAdminRole() {
        verifyBusinessProfiles(PROFILE_ROLE_ADMIN, false, new boolean[] {true, true, true});
        verifyRetailProfile(PROFILE_ROLE_ADMIN, false, false);
    }

    @Test
    public void testBusinessEmployeeRole() {
        verifyBusinessProfiles(PROFILE_ROLE_EMPLOYEE, false, new boolean[] {true, false, false});
        verifyRetailProfile(PROFILE_ROLE_EMPLOYEE, false, false);
    }

    @Test
    public void testRetailAdminRole() {
        verifyBusinessProfiles(PROFILE_ROLE_ADMIN, true, new boolean[] {false, false, true});
        verifyRetailProfile(PROFILE_ROLE_ADMIN, true, true);
    }

    @Test
    public void testRetailUserRole() {
        verifyBusinessProfiles(PROFILE_ROLE_RETAIL, true, new boolean[] {false, false, false});
        verifyRetailProfile(PROFILE_ROLE_RETAIL, true, true);
    }

    @Test
    public void testApplicableJobProfileTemplatesForBusiness() {
        final boolean isRetail = false;
        JobProfile profile = createRetailUserProfile();
        boolean isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should not be applicable to business",
            isApplicable, is(false));

        profile = createAdminProfile();
        isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should be applicable to business",
            isApplicable, is(true));

        profile = createFinanceEmployeeProfile();
        isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should be applicable to business",
            isApplicable, is(true));

        profile = createManagerProfile();
        isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should be applicable to business",
            isApplicable, is(true));
    }

    @Test
    public void testApplicableJobProfileTemplatesForRetail() {
        final boolean isRetail = true;
        JobProfile profile = createRetailUserProfile();
        boolean isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should be applicable to retail", isApplicable, is(true));

        profile = createAdminProfile();
        isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should be applicable to retail", isApplicable, is(true));

        profile = createFinanceEmployeeProfile();
        isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should not be applicable to retail",
            isApplicable, is(false));

        profile = createManagerProfile();
        isApplicable = this.subject.isJobProfileForBranch(isRetail, profile);
        assertThat(profile.getJobProfileName() + " should not be applicable to retail",
            isApplicable, is(false));
    }
}
