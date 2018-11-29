package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.JOB_PROFILE_NAME_SUPPORT;
import static com.backbase.ct.bbfuel.service.factory.LegalEntityWithUsersFactory.createLegalEntityWithUsers;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityWithUsersServiceTest {
    @InjectMocks
    private LegalEntityWithUsersService subject;

    @Test
    public void testFilterLegalEntitiesWithoutSupport() {
        List<LegalEntityWithUsers> lewus = asList(
            createLegalEntityWithUsers(asList("1", "2")),
            createLegalEntityWithUsers(asList("11", "12")),
            createLegalEntityWithUsers(asList("21", "22")),
            createLegalEntityWithUsers(asList("31", "32")),
            createLegalEntityWithUsers(asList("41", "42")));
        lewus.forEach(legalEntityWithUsers -> {
            legalEntityWithUsers.getUsers().forEach(user -> user.setJobProfileNames(asList("Finance")));
        });
        lewus.get(0).getUsers().get(0).setJobProfileNames(asList(JOB_PROFILE_NAME_SUPPORT, "Finance"));
        assertThat("one user should have Support profile",
            lewus.get(0).getUsers().get(0).getJobProfileNames().get(0), is(JOB_PROFILE_NAME_SUPPORT));

        List<LegalEntityWithUsers> noSupport = subject.filterLegalEntitiesNotHavingSupportEmployees(lewus);

        assertThat("LEs with one or more Support employees should not be present ", noSupport, hasSize(lewus.size() - 1));
        assertThat("filtered user should not have Support profile",
            noSupport.get(0).getUsers().get(0).getJobProfileNames(), not(contains(JOB_PROFILE_NAME_SUPPORT)));
    }

    @Test
    public void testFilterLegalEntitiesWithoutCustom() {
        List<LegalEntityWithUsers> lewus = asList(
            createLegalEntityWithUsers(asList("1", "2")),
            createLegalEntityWithUsers(asList("11", "12")),
            createLegalEntityWithUsers(asList("21", "22")),
            createLegalEntityWithUsers(asList("31", "32")),
            createLegalEntityWithUsers(asList("41", "42")));
        lewus.forEach(legalEntityWithUsers -> {
            legalEntityWithUsers.getUsers().forEach(user -> user.setJobProfileNames(asList("Finance")));
        });
        lewus.get(0).getUsers().get(0).setJobProfileNames(asList(JOB_PROFILE_NAME_SUPPORT, "Finance"));

        List<LegalEntityWithUsers> noSupport = subject
            .filterLegalEntitiesNotHavingAssignedJobProfiles(lewus, asList("Finance"));

        assertThat("LEs with one or more Support employees should not be present ", noSupport, hasSize(0));
    }
}
