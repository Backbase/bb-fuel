package com.backbase.ct.dataloader.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.github.javafaker.Faker;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JobProfileServiceTest {

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
        expectedException.expectMessage(containsString("No jobProfile found"));
        this.subject.findByApprovalLevelAndExternalServiceAgreementId(
            "D", EXTERNAL_SERVICE_AGREEMENT_ID_1);
    }
}
