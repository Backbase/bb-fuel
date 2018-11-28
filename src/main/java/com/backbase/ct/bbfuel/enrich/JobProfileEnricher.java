package com.backbase.ct.bbfuel.enrich;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JobProfileEnricher {

    /**
     * Enrich each job profile.
     * @param jobProfiles a list of job profiles
     */
    public void enrich(List<JobProfile> jobProfiles) {
        jobProfiles.forEach(this::enrich);
    }

    /**
     * Set isRetail to false if not set.
     *
     * @param jobProfile job profile
     */
    private void enrich(JobProfile jobProfile) {
        if (jobProfile.getIsRetail() == null) {
            jobProfile.setIsRetail(false);
        }
    }
}
