package com.backbase.ct.dataloader.input;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.enrich.JobProfileEnricher;
import java.util.List;
import java.util.Objects;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobProfileReaderTest {

    @InjectMocks
    private JobProfileReader subject;

    @Mock
    private JobProfileEnricher jobProfileEnricher;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testLoadJobProfiles() {
        List<JobProfile> jobProfiles = subject.load();
        assertThat(jobProfiles, hasSize(4));
        assertThat("permission should have business function",
            jobProfiles.get(1).getPermissions().get(0).getBusinessFunction(), notNullValue());
        assertThat("profile should have enough permissions",
            jobProfiles.get(2).getPermissions().size(), greaterThan(20));
        assertThat("Only one specific profile for retail expected", jobProfiles.stream()
            .filter(jobProfile -> Objects.nonNull(jobProfile.getIsRetail()))
            .filter(jobProfile -> jobProfile.getIsRetail())
            .collect(toList()), hasSize(1));
    }

    @Test
    public void testInvalidFileFails() {
        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(containsString("Unrecognized field"));

        List<JobProfile> jobProfiles = subject.load("data/products.json");
    }
}
