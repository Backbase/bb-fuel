package com.backbase.testing.dataloader.configurators;


import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.data.DataAccessGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class PermissionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsConfigurator.class);

    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();

    public void assignAllFunctionDataGroupsToMasterServiceAgreementAndUser(String externalLegalEntityId, String externalUserId) {
        String internalLegalEntityId = legalEntityPresentationRestClient.retrieveLegalEntityByExternalId(externalLegalEntityId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(LegalEntityByExternalIdGetResponseBody.class)
                .getId();

        FunctionAccessGroupsGetResponseBody[] functionGroups = accessGroupPresentationRestClient.retrieveFunctionGroupsByLegalEntity(internalLegalEntityId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionAccessGroupsGetResponseBody[].class);

        DataAccessGroupsGetResponseBody[] dataGroups = accessGroupPresentationRestClient.retrieveDataGroupsByLegalEntityAndType(internalLegalEntityId, "arrangements")
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(DataAccessGroupsGetResponseBody[].class);

        List<String> dataGroupIds = new ArrayList<>();
        Arrays.stream(dataGroups)
                .forEach(dg -> dataGroupIds.add(dg.getDataAccessGroupId()));

        for (FunctionAccessGroupsGetResponseBody functionGroup : functionGroups) {
            accessGroupIntegrationRestClient.assignPermissions(externalLegalEntityId, externalUserId, null, functionGroup.getFunctionAccessGroupId(), dataGroupIds)
                    .then()
                    .statusCode(SC_OK);
            LOGGER.info(String.format("Permission assigned for legal entity [%s], user [%s], master service agreement, function group [%s], data groups %s", externalLegalEntityId, externalUserId, functionGroup.getFunctionAccessGroupId(), dataGroupIds));
        }
    }
}