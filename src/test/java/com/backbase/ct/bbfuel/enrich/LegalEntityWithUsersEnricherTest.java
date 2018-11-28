package com.backbase.ct.bbfuel.enrich;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.JOB_PROFILE_NAME_ADMIN;
import static com.backbase.ct.bbfuel.service.factory.LegalEntityWithUsersFactory.createLegalEntityWithUsers;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityWithUsersEnricherTest {

    @InjectMocks
    private LegalEntityWithUsersEnricher subject;

    @Test
    public void testEnrich() {
        List<LegalEntityWithUsers> legalEntities = singletonList(
            createLegalEntityWithUsers("U0001"));

        subject.enrich(legalEntities);

        legalEntities.forEach(le -> {
            assertThat("Missing users for legalEntity " + le,
                le.getUsers(), hasSize(greaterThan(0)));
        });
    }

    @Test
    public void testUsernameConversion() {
        LegalEntityWithUsers le = createLegalEntityWithUsers("alice.johnson_IS_NICE");

        subject.enrich(singletonList(le));

        assertThat(le.getUsers().get(0).getFullName(), is("Alice Johnson Is Nice"));
    }

    @Test
    public void testUserJobProfiles() {
        LegalEntityWithUsers legalEntityWithUsers = createLegalEntityWithUsers("U0001");

        subject.enrich(singletonList(legalEntityWithUsers));
        assertThat(legalEntityWithUsers.getUsers().get(0).getJobProfileNames(), hasSize(1));
        assertThat(legalEntityWithUsers.getUsers().get(0).getJobProfileNames().get(0), is(JOB_PROFILE_NAME_ADMIN));
        subject.enrich(singletonList(legalEntityWithUsers));
        assertThat(legalEntityWithUsers.getUsers().get(0).getJobProfileNames(), hasSize(1));
        assertThat(legalEntityWithUsers.getUsers().get(0).getJobProfileNames().get(0), is(JOB_PROFILE_NAME_ADMIN));
    }
}
