package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermissionsForJobProfile;
import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermissionsWithAllPrivileges;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.service.AccessGroupService;
import com.backbase.ct.dataloader.service.JobProfileService;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessGroupsConfigurator {

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    private final AccessGroupService accessGroupService;

//    private Map<String, String> functionGroupCache = synchronizedMap(new HashMap<>());
    private final JobProfileService jobProfileService;

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    private static final String ADMIN_FUNCTION_GROUP_NAME = "Admin";

    public JobProfile ingestAdminFunctionGroup(String externalServiceAgreementId) {
        JobProfile adminProfile = new JobProfile(ADMIN_FUNCTION_GROUP_NAME, null, null);
        adminProfile.setExternalServiceAgreementId(externalServiceAgreementId);
        ingestFunctionGroup(adminProfile);
        return adminProfile;
    }

    /**
     * Ingest a function group aka job profile.
     * A profile without explicit permissions will be granted all.
     */
    public synchronized String ingestFunctionGroup(JobProfile jobProfile) {
        List<FunctionsGetResponseBody> functions = this.accessGroupIntegrationRestClient.retrieveFunctions();

        String functionGroupId = jobProfileService.retrieveIdFromCache(jobProfile);
        if (functionGroupId != null) {
            return functionGroupId;
        }
        List<Permission> permissions = jobProfile.getPermissions() == null
            ? createPermissionsWithAllPrivileges(functions)
            : createPermissionsForJobProfile(jobProfile, functions);

        functionGroupId = accessGroupService.ingestFunctionGroup(jobProfile.getExternalServiceAgreementId(),
            jobProfile.getJobProfileName(), permissions);
        jobProfile.setId(functionGroupId);

        jobProfileService.storeInCache(jobProfile);

        return functionGroupId;
    }

    public String ingestDataGroupForArrangements(String externalServiceAgreementId, String dataGroupName,
        List<ArrangementId> arrangementIds) {
        List<String> internalArrangementIds = arrangementIds.stream()
            .map(ArrangementId::getInternalArrangementId)
            .collect(toList());

        return accessGroupService.ingestDataGroup(
            externalServiceAgreementId, dataGroupName, ARRANGEMENTS, internalArrangementIds);
    }
}
