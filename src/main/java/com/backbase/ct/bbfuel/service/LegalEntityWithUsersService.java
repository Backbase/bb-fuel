package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.JOB_PROFILE_NAME_SUPPORT;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Utility service to handle filtering of {@link LegalEntityWithUsers}.
 * Note: the filter methods should be more fine grained as they do not filter out the users but the whole Legal Entity
 * that does not match the criteria.
 */
@Slf4j
@Service
public class LegalEntityWithUsersService {

    /**
     * Only return those legalEntitiesWithUsers that have no "Support Employee" users at all.
     *
     * @param legalEntitiesWithUsers list of legalEntitiesWithUsers
     * @return filtered list of legalEntitiesWithUsers that do not match the support job profile
     */
    public List<LegalEntityWithUsers> filterLegalEntitiesNotHavingSupportEmployees(
        List<LegalEntityWithUsers> legalEntitiesWithUsers) {
        return filterLegalEntitiesNotHavingAssignedJobProfile(legalEntitiesWithUsers, JOB_PROFILE_NAME_SUPPORT);
    }

    /**
     * Only return those legalEntitiesWithUsers that have no single user matching the jobProfileName.
     *
     * @param legalEntitiesWithUsers list of legalEntitiesWithUsers
     * @param jobProfileName that should be filtered out for the users
     * @return filtered list of legalEntitiesWithUsers that do not match the jobProfileName
     */
    public List<LegalEntityWithUsers> filterLegalEntitiesNotHavingAssignedJobProfile(
        List<LegalEntityWithUsers> legalEntitiesWithUsers,
        String jobProfileName) {
        return legalEntitiesWithUsers
            .stream()
            .filter(legalEntities -> legalEntities.getUsers()
                .stream()
                .noneMatch(user -> user.getJobProfileNames().contains(jobProfileName)))
            .collect(Collectors.toList());

    }

    /**
     * Only return those LegalEntitiesWithUsers that have no single user matching one or more jobProfileNames.
     *
     * @param legalEntitiesWithUsers list of LegalEntitiesWithUsers
     * @param jobProfileNames that should be filtered out for the users
     * @return filtered list of LegalEntitiesWithUsers that do not match any of the jobProfileNames
     */
    public List<LegalEntityWithUsers> filterLegalEntitiesNotHavingAssignedJobProfiles(
        List<LegalEntityWithUsers> legalEntitiesWithUsers,
        List<String> jobProfileNames) {
        return legalEntitiesWithUsers
            .stream()
            .filter(legalEntities -> legalEntities.getUsers()
                .stream()
                .anyMatch(user -> Collections.disjoint(user.getJobProfileNames(), jobProfileNames)
                ))
            .collect(Collectors.toList());
    }
}
