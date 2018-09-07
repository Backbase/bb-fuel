package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermissionsWithAllPrivileges;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.service.AccessGroupService;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessGroupsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupsConfigurator.class);

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    private final AccessGroupService accessGroupService;

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    private static final String ADMIN_FUNCTION_GROUP_NAME = "Admin";

    public String ingestAdminFunctionGroup(String externalServiceAgreementId) {
        return ingestAdminFunctionGroup(externalServiceAgreementId, ADMIN_FUNCTION_GROUP_NAME);
    }

    public String ingestAdminFunctionGroup(String externalServiceAgreementId, String functionGroupName) {
        List<FunctionsGetResponseBody> functions = this.accessGroupIntegrationRestClient
            .retrieveFunctions();

        return ingestFunctionGroupWithAllPrivileges(externalServiceAgreementId, functionGroupName, functions);
    }

    private synchronized String ingestFunctionGroupWithAllPrivileges(String externalServiceAgreementId,
        String functionGroupName, List<FunctionsGetResponseBody> functions) {

        return accessGroupService.ingestFunctionGroup(externalServiceAgreementId,
            functionGroupName, createPermissionsWithAllPrivileges(functions));
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
