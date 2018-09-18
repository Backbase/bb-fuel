package com.backbase.ct.dataloader.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobProfileReaderTest {

    @InjectMocks
    private JobProfileReader subject;

    @Test
    public void testLoadBusinessBanking() {
        List<JobProfile> jobProfiles = subject.load();
        assertThat(jobProfiles, hasSize(2));
        assertThat("profile should have enough permissions",
            jobProfiles.get(0).getPermissions().size(), greaterThan(20));
        assertThat("permission should have business function",
            jobProfiles.get(0).getPermissions().get(0).getBusinessFunction(), notNullValue());
    }

    @Test
    public void testLoadRetail() {
        List<JobProfile> jobProfiles = subject.load("data/job-profiles-retail.json");
        assertThat(jobProfiles, hasSize(2));
        assertThat("profile should have enough permissions",
            jobProfiles.get(0).getPermissions().size(), greaterThan(20));
        assertThat("permission should have business function",
            jobProfiles.get(0).getPermissions().get(0).getBusinessFunction(), notNullValue());
    }
}
