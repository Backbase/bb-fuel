package com.backbase.ct.bbfuel.input.validation;

import static com.backbase.ct.bbfuel.service.factory.JobProfileFactory.JOB_PROFILE_NAME_MANAGER;
import static com.backbase.ct.bbfuel.service.factory.JobProfileFactory.createJobProfiles;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.input.InvalidInputException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobProfileAssignmentValidatorTest {

    @InjectMocks
    private JobProfileAssignmentValidator subject;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static LegalEntityWithUsers createLegalEntityWithUsers(List<String> jobProfileNames) {
        User user = User.builder().externalId("U0001").jobProfileNames(
            jobProfileNames).build();
        LegalEntityWithUsers legalEntity = new LegalEntityWithUsers();
        legalEntity.setUsers(singletonList(user));
        return legalEntity;
    }

    @Test
    public void verifyExistingJobProfile() {
        List<String> jobProfileNames = singletonList(JOB_PROFILE_NAME_MANAGER);
        LegalEntityWithUsers legalEntityWithUsers = createLegalEntityWithUsers(jobProfileNames);
        this.subject.verify(singletonList(legalEntityWithUsers), createJobProfiles(jobProfileNames));
    }

    @Test
    public void verifyNonExistingJobProfiles() {
        List<String> nonExistant = asList("Local Trader", "Community Trader");
        LegalEntityWithUsers legalEntityWithUsers = createLegalEntityWithUsers(nonExistant);

        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage("User U0001 has been assigned non existing job profile: "
            + nonExistant);
        this.subject.verify(singletonList(legalEntityWithUsers),
            createJobProfiles(singletonList(JOB_PROFILE_NAME_MANAGER)));
    }
}