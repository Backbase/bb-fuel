package com.backbase.ct.dataloader.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedMap;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(JobProfileService.class);

    private static final Map<String, List<String>> ROLE_APPROVAL_LEVELS;

    static {
        // very inflexible but suits our needs for now
        ROLE_APPROVAL_LEVELS = new HashMap<>();
        ROLE_APPROVAL_LEVELS.put("admin", asList("A,B,C".split(",")));
        ROLE_APPROVAL_LEVELS.put("manager", asList("A,B".split(",")));
        ROLE_APPROVAL_LEVELS.put("employee", singletonList("A"));
    }

    private Map<String, List<JobProfile>> assignedJobProfiles = new HashMap<>();

    private Map<String, String> functionGroupCache = synchronizedMap(new HashMap<>());

    private static String createCacheKey(JobProfile jobProfile) {
        return String.format("%s-%s", jobProfile.getExternalServiceAgreementId(),
            deleteWhitespace(jobProfile.getJobProfileName()).trim());
    }

    /**
     * Mock data model assumes a one-to-one relationship for Job profile and approval level. Role and Job profile have a
     * one-to-many relationship. This can bite if approvals is not enabled in a business banking environment.
     *
     * @return true if jobProfile has no approval level or the config contains this approval level for given role
     */
    public boolean isJobProfileForUserRole(JobProfile jobProfile, String role) {
        if (jobProfile.getApprovalLevel() == null) {
            // the model assumes approvals enabled for business banking, so implicitly this is a profile for retail
            return true;
        }

        List<String> approvalLevels = ROLE_APPROVAL_LEVELS.get(role.toLowerCase());
        // custom role is not validated so
        if (approvalLevels == null || approvalLevels.isEmpty()) {
            LOGGER.warn("Role has no configured approval levels (so this user will not be assigned any job profile)");
            return false;
        }
        return approvalLevels.contains(jobProfile.getApprovalLevel());
    }

    public List<JobProfile> getAssignedJobProfiles(String externalServiceAgreementId) {
        return assignedJobProfiles.get(externalServiceAgreementId);
    }

    public boolean saveAssignedProfile(JobProfile jobProfile) {
        List<JobProfile> profiles = this.assignedJobProfiles.get(jobProfile.getExternalServiceAgreementId());
        if (profiles == null) {
            profiles = new ArrayList<>();
            this.assignedJobProfiles.put(jobProfile.getExternalServiceAgreementId(), profiles);
        }
        return profiles.add(jobProfile);
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
                new IllegalStateException(String.format("No jobProfile found for level [%s] and externalSaId [%s]",
                    approvalLevel, externalServiceAgreementId)));
    }

    public String retrieveIdFromCache(JobProfile jobProfile) {
        String cacheKey = createCacheKey(jobProfile);

        if (functionGroupCache.containsKey(cacheKey)) {
            return functionGroupCache.get(cacheKey);
        }

        return null;
    }

    public void storeInCache(JobProfile jobProfile) {
        functionGroupCache.put(createCacheKey(jobProfile), jobProfile.getId());
    }
}
