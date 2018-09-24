package com.backbase.ct.dataloader.enrich;

import static com.backbase.ct.dataloader.dto.entitlement.JobProfile.PROFILE_ROLE_ADMIN;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JobProfileEnricher {

    /**
     * Check if roles are filled and add admin to it if not.
     */
    public void enrich(List<JobProfile> jobProfiles) {
        jobProfiles.forEach(this::enrichCategories);
    }

    private void enrichCategories(JobProfile jobProfile) {
        if (jobProfile.getRoles() == null) {
            jobProfile.setRoles(new ArrayList<>());
        }
        if (jobProfile.getRoles().isEmpty()) {
            jobProfile.getRoles().add(PROFILE_ROLE_ADMIN);
        }

        if (jobProfile.getIsRetail() == null) {
            jobProfile.setIsRetail(false);
        }
    }
}
