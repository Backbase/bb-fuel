package com.backbase.ct.bbfuel.service.factory;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience factory for creation of {@link JobProfile}.
 */
public class JobProfileFactory {

    public static final String JOB_PROFILE_NAME_MANAGER = "Manager";

    /**
     * Create one job profile for each name in jobProfileNames.
     *
     * @param jobProfileNames names for the job profiles to be created
     * @return list of job profiles
     */
    public static List<JobProfile> createJobProfiles(List<String> jobProfileNames) {
        List<JobProfile> jobProfiles = new ArrayList<>();
        jobProfileNames.forEach(name -> jobProfiles.add(createJobProfile(name)));
        return jobProfiles;
    }

    /**
     * Create a job profile for given jobProfileName.
     *
     * @param jobProfileName name for the new job profile
     * @return new job profile
     */
    public static JobProfile createJobProfile(String jobProfileName) {
        return JobProfile.builder()
            .jobProfileName(jobProfileName)
            .build();
    }
}
