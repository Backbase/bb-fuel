package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.dto.entitlement.JobProfile.PROFILE_ROLE_ADMIN;
import static java.util.Collections.synchronizedMap;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A simple local service with no integration at all.
 */
@Service
@RequiredArgsConstructor
public class JobProfileService {

    public static final String ADMIN_FUNCTION_GROUP_NAME = "Admin";

    private static final Logger LOGGER = LoggerFactory.getLogger(JobProfileService.class);

    private Map<String, List<JobProfile>> assignedJobProfiles = new HashMap<>();

    private Map<String, String> functionGroupCache = synchronizedMap(new HashMap<>());

    private static String createCacheKey(JobProfile jobProfile) {
        return String.format("%s-%s", jobProfile.getExternalServiceAgreementId(),
            deleteWhitespace(jobProfile.getJobProfileName()).trim());
    }

    public boolean isJobProfileForBranch(boolean isRetail, JobProfile template) {
        return (this.isJobProfileForAdmin(template)
            || (template.getIsRetail() && isRetail
            || (!isRetail && !template.getIsRetail())));
    }

    /**
     * Admin is a special case for job profile.
     */
    public boolean isJobProfileForAdmin(JobProfile jobProfile) {
        return ADMIN_FUNCTION_GROUP_NAME.equalsIgnoreCase(jobProfile.getJobProfileName())
            && jobProfile.getRoles().contains(PROFILE_ROLE_ADMIN);
    }

    /**
     * Evaluate jobProfile roles, its name and isRetail property to given rol and isRetailCustomer.
     */
    public boolean isJobProfileForUserRole(JobProfile jobProfile, String role, boolean isRetailCustomer) {
        List<String> roles = jobProfile.getRoles();

        if (roles == null || roles.isEmpty()) {
            LOGGER.warn("No roles configured for this profile {}", jobProfile.getJobProfileName());
            return false;
        } else if (isRetailCustomer) {
            return (jobProfile.getIsRetail() && roles.contains(role))
            || (!jobProfile.getIsRetail() // admin profile is shared between retail and business
                && ADMIN_FUNCTION_GROUP_NAME.equalsIgnoreCase(jobProfile.getJobProfileName())
                && roles.contains(role));
        }
        return !jobProfile.getIsRetail() && roles.contains(role);
    }

    public List<JobProfile> getAssignedJobProfiles(String externalServiceAgreementId) {
        return assignedJobProfiles.get(externalServiceAgreementId);
    }

    public void saveAssignedProfile(JobProfile jobProfile) {
        this.assignedJobProfiles
            .computeIfAbsent(
                jobProfile.getExternalServiceAgreementId(), key -> new ArrayList<>())
            .add(jobProfile);
    }

    public JobProfile findByApprovalLevelAndExternalServiceAgreementId(
        String approvalLevel, String externalServiceAgreementId) {
        List<JobProfile> jobProfiles = assignedJobProfiles.get(externalServiceAgreementId);
        if (jobProfiles == null) {
            return null;
        }
        return jobProfiles.stream()
            .filter(jobProfile -> approvalLevel.equals(jobProfile.getApprovalLevel()))
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(String.format("No job profile found for level [%s] and external service agreement id [%s]",
                    approvalLevel, externalServiceAgreementId)));
    }

    public String retrieveIdFromCache(JobProfile jobProfile) {
        return functionGroupCache.get(createCacheKey(jobProfile));
    }

    public void storeInCache(JobProfile jobProfile) {
        functionGroupCache.put(createCacheKey(jobProfile), jobProfile.getId());
    }
}
