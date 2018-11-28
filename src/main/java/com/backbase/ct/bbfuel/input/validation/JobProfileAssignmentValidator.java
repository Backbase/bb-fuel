package com.backbase.ct.bbfuel.input.validation;

import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.ct.bbfuel.input.InvalidInputException;
import java.util.List;
import org.apache.commons.collections.ListUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class JobProfileAssignmentValidator {

    /**
     * Verify whether the assigned job profiles groups for each legal entity match up with given jobProfiles.
     *
     * @param legalEntities a list of legal entity with their direct users
     * @param jobProfiles a list of product group objects
     */
    public void verify(List<LegalEntityWithUsers> legalEntities, List<JobProfile> jobProfiles) {
        List<String> jobProfileNames = jobProfiles.stream()
            .map(JobProfile::getJobProfileName)
            .collect(toList());

        legalEntities.forEach(legalEntityWithUsers -> verify(legalEntityWithUsers, jobProfileNames));
    }

    /**
     * Verify each user.
     *
     * @param legalEntityWithUsers a legal entity with its direct users
     * @param jobProfileNames a list of job profile names
     */
    private void verify(LegalEntityWithUsers legalEntityWithUsers, List<String> jobProfileNames) {
        legalEntityWithUsers.getUsers().forEach(user -> verify(user, jobProfileNames));
    }

    /**
     * Verify whether the assigned job profiles for each user do exist.
     *
     * @param user a user
     * @param jobProfileNames a list of job profile names
     */
    private void verify(User user, List<String> jobProfileNames) {
        List<String> userJobProfileNames = user.getJobProfileNames();
        if (!CollectionUtils.isEmpty(userJobProfileNames)
            && !jobProfileNames.containsAll(userJobProfileNames)) {
            throw new InvalidInputException(String.format("User %s has been assigned non existing job profile: %s",
                user.getExternalId(), ListUtils.subtract(userJobProfileNames, jobProfileNames)));
        }
    }
}
