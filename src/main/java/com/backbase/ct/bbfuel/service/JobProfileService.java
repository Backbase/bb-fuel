package com.backbase.ct.bbfuel.service;

import static java.util.Collections.synchronizedMap;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A simple local service with no integration at all.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobProfileService {

    private Map<String, List<JobProfile>> assignedJobProfiles = new HashMap<>();

    private Map<String, String> functionGroupCache = synchronizedMap(new HashMap<>());

    private static String createCacheKey(JobProfile jobProfile) {
        return String.format("%s-%s", jobProfile.getExternalServiceAgreementId(),
            deleteWhitespace(jobProfile.getJobProfileName()).trim());
    }

    public boolean isJobProfileForBranch(boolean isRetail, JobProfile template) {
        return (JobProfile.JOB_PROFILE_NAME_ADMIN.equalsIgnoreCase(template.getJobProfileName())
            || (template.getIsRetail() && isRetail
            || (!isRetail && !template.getIsRetail())));
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
