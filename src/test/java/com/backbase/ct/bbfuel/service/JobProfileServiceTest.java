package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.service.factory.TestData.EXTERNAL_SERVICE_AGREEMENT_ID_1;
import static com.backbase.ct.bbfuel.service.factory.TestData.EXTERNAL_SERVICE_AGREEMENT_ID_2;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JobProfileServiceTest {
    private static final String APPROVAL_LEVEL_A = "A";
    private static final String APPROVAL_LEVEL_B = "B";
    private static final String APPROVAL_LEVEL_C = "C";
    private static final List<String> APPROVAL_LEVELS = asList(APPROVAL_LEVEL_A, APPROVAL_LEVEL_B, APPROVAL_LEVEL_C);

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
        String name, String approvalLevel, boolean isRetailProfile) {
        return JobProfile.builder()
            .jobProfileName(name)
            .approvalLevel(approvalLevel)
            .isRetail(isRetailProfile)
            .build();
    }

    private static JobProfile createAdminProfile() {
        return createJobProfile("Admin", APPROVAL_LEVEL_C, false);
    }

    private static JobProfile createManagerProfile() {
        return createJobProfile("Manager", APPROVAL_LEVEL_B, false);
    }

    private static JobProfile createFinanceEmployeeProfile() {
        return createJobProfile("Finance employee", APPROVAL_LEVEL_A, false);
    }

    private static JobProfile createRetailUserProfile() {
        return createJobProfile("Retail User", null,true);
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
