package com.backbase.ct.dataloader.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobProfileReaderTest {

    @InjectMocks
    private JobProfileReader subject;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testLoadBusinessBanking() {
        List<JobProfile> jobProfiles = subject.load();
        assertThat(jobProfiles, hasSize(3));
        assertThat("profile should have enough permissions",
            jobProfiles.get(1).getPermissions().size(), greaterThan(20));
        assertThat("permission should have business function",
            jobProfiles.get(1).getPermissions().get(0).getBusinessFunction(), notNullValue());
    }

    @Test
    public void testLoadRetail() {
        List<JobProfile> jobProfiles = subject.load("data/job-profiles-retail.json");
        assertThat(jobProfiles, hasSize(2));
        assertThat("profile is for admin (permissions will be generated)",
            jobProfiles.get(0).getPermissions(), is(nullValue()));
        assertThat("profile should have enough permissions",
            jobProfiles.get(1).getPermissions().size(), greaterThan(10));
        assertThat("permission should have business function",
            jobProfiles.get(1).getPermissions().get(0).getBusinessFunction(), notNullValue());
    }

    @Test
    public void testInvalidFileFails() {

        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(containsString("Unrecognized field"));

        List<JobProfile> jobProfiles = subject.load("data/products.json");
    }
}
