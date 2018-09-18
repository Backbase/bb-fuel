package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermissionsWithAllPrivileges;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.service.AccessGroupService;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessGroupsConfigurator {

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    private final AccessGroupService accessGroupService;

    private Map<String, String> functionGroupCache = synchronizedMap(new HashMap<>());

    List<FunctionsGetResponseBody> functions;

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    private static final String ADMIN_FUNCTION_GROUP_NAME = "Admin";

    public String ingestAdminFunctionGroup(String externalServiceAgreementId) {
        return ingestAdminFunctionGroup(externalServiceAgreementId, ADMIN_FUNCTION_GROUP_NAME);
    }

    public String ingestAdminFunctionGroup(String externalServiceAgreementId, String functionGroupName) {
        if (functions == null) {
            functions = this.accessGroupIntegrationRestClient.retrieveFunctions();
        }

        return ingestFunctionGroupWithAllPrivileges(externalServiceAgreementId, functionGroupName, functions);
    }

    /**
     * TODO call this
     */
    private synchronized String ingestJobProfileWithPrivileges(String externalServiceAgreementId,
        JobProfile jobProfile, List<FunctionsGetResponseBody> functions) {
        String cacheKey = String.format("%s-%s", externalServiceAgreementId,
            deleteWhitespace(jobProfile.getJobProfileName()).trim());

        if (functionGroupCache.containsKey(cacheKey)) {
            return functionGroupCache.get(cacheKey);
        }

        String functionGroupId = accessGroupService.ingestFunctionGroup(externalServiceAgreementId,
            jobProfile.getJobProfileName(), createPermissionsWithAllPrivileges(functions));

        functionGroupCache.put(cacheKey, functionGroupId);

        return functionGroupId;
    }

    private synchronized String ingestFunctionGroupWithAllPrivileges(String externalServiceAgreementId,
        String functionGroupName, List<FunctionsGetResponseBody> functions) {
        String cacheKey = String.format("%s-%s", externalServiceAgreementId, deleteWhitespace(functionGroupName).trim());

        if (functionGroupCache.containsKey(cacheKey)) {
            return functionGroupCache.get(cacheKey);
        }

        String functionGroupId = accessGroupService.ingestFunctionGroup(externalServiceAgreementId,
            functionGroupName, createPermissionsWithAllPrivileges(functions));

        functionGroupCache.put(cacheKey, functionGroupId);

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
