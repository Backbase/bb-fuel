package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.AccessGroupsDataGenerator.createPermissionsForJobProfile;
import static com.backbase.ct.bbfuel.data.AccessGroupsDataGenerator.createPermissionsWithAllPrivileges;
import static com.backbase.ct.bbfuel.service.JobProfileService.ADMIN_FUNCTION_GROUP_NAME;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.bbfuel.dto.ArrangementId;
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.service.AccessGroupService;
import com.backbase.ct.bbfuel.service.JobProfileService;
import com.backbase.ct.bbfuel.service.ProductGroupService;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.FunctionsGetResponseBody;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.Permission;
import com.backbase.dbs.accesscontrol.client.v3.model.FunctionGroupType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessGroupsConfigurator {

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    private final AccessGroupService accessGroupService;

    private final JobProfileService jobProfileService;

    private final ProductGroupService productGroupService;

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    public JobProfile ingestAdminFunctionGroup(String externalServiceAgreementId) {
        JobProfile adminProfile = new JobProfile(ADMIN_FUNCTION_GROUP_NAME, null, null, null,
            null, FunctionGroupType.REGULAR.toString());
        adminProfile.setExternalServiceAgreementId(externalServiceAgreementId);
        ingestFunctionGroup(adminProfile);
        return adminProfile;
    }

    /**
     * Ingest a function group aka job profile.
     * A profile without explicit permissions will be granted all.
     */
    public synchronized void ingestFunctionGroup(JobProfile jobProfile) {
        List<FunctionsGetResponseBody> functions = this.accessGroupIntegrationRestClient.retrieveFunctions();

        String functionGroupId = jobProfileService.retrieveIdFromCache(jobProfile);
        if (functionGroupId != null) {
            return;
        }
        List<Permission> permissions = jobProfile.getPermissions() == null
            ? createPermissionsWithAllPrivileges(functions)
            : createPermissionsForJobProfile(jobProfile, functions);

        functionGroupId = accessGroupService.ingestFunctionGroup(jobProfile.getExternalServiceAgreementId(),
            jobProfile.getJobProfileName(), jobProfile.getType(), permissions);
        jobProfile.setId(functionGroupId);

        jobProfileService.storeInCache(jobProfile);
    }

    public synchronized void ingestDataGroupForArrangements(ProductGroupSeed productGroupSeed,
        List<ArrangementId> arrangementIds) {
        List<String> internalArrangementIds = arrangementIds.stream()
            .map(ArrangementId::getInternalArrangementId)
            .toList();

        String dataGroupId = productGroupService.retrieveIdFromCache(productGroupSeed);
        if (dataGroupId != null) {
            return;
        }

        dataGroupId = accessGroupService.ingestDataGroup(productGroupSeed.getExternalServiceAgreementId(),
            productGroupSeed.getProductGroupName(), ARRANGEMENTS, internalArrangementIds);
        productGroupSeed.setId(dataGroupId);

        productGroupService.saveAssignedProductGroup(productGroupSeed);
    }
}
