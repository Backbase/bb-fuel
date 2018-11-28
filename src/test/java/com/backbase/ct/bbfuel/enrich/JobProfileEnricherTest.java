package com.backbase.ct.bbfuel.enrich;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.JOB_PROFILE_NAME_ADMIN;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobProfileEnricherTest {

    @InjectMocks
    private JobProfileEnricher subject;

    @Test
    public void testEnrichmentAddsDefault() {
        JobProfile jobProfile = new JobProfile();
        List<JobProfile> jobProfiles = singletonList(jobProfile);

        assertThat(jobProfile.getIsRetail(), nullValue());
        subject.enrich(jobProfiles);
        assertThat(jobProfile.getIsRetail(), is(false));
        subject.enrich(jobProfiles);
        assertThat(jobProfile.getIsRetail(), is(false));
    }
}
