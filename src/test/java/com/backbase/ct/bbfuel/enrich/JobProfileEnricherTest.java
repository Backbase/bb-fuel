package com.backbase.ct.bbfuel.enrich;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.PROFILE_ROLE_ADMIN;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobProfileEnricherTest {

    @InjectMocks
    private JobProfileEnricher subject;

    @Test
    public void testEnrichmentAddsDefault() {
        JobProfile jobProfile = new JobProfile();
        List<JobProfile> jobProfiles = singletonList(jobProfile);

        assertThat(jobProfile.getRoles(), nullValue());
        subject.enrich(jobProfiles);
        assertThat(jobProfile.getRoles(), hasSize(1));
        assertThat(jobProfile.getRoles().get(0), is(PROFILE_ROLE_ADMIN));
        subject.enrich(jobProfiles);
        assertThat(jobProfile.getRoles(), hasSize(1));
    }

    @Test
    public void testEnrichmentDoesNotAdd() {
        JobProfile jobProfile = JobProfile.builder().roles(singletonList(PROFILE_ROLE_ADMIN)).build();
        List<JobProfile> jobProfiles = singletonList(jobProfile);

        subject.enrich(jobProfiles);
        assertThat(jobProfile.getRoles(), hasSize(1));
        assertThat(jobProfile.getRoles().get(0), is(PROFILE_ROLE_ADMIN));
    }

}
