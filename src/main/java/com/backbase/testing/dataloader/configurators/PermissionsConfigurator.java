package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class PermissionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsConfigurator.class);

    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();

    public void assignAllFunctionDataGroupsOfLegalEntityToUserAndServiceAgreement(String externalLegalEntityId, String externalUserId, String internalServiceAgreementId) {
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

        List<String> dataGroupIds = accessGroupPresentationRestClient.retrieveAllDataGroupIdsByLegalEntity(internalLegalEntityId);

        for (FunctionAccessGroupsGetResponseBody functionGroup : functionGroups) {
            accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
                    .withExternalLegalEntityId(externalLegalEntityId)
                    .withExternalUserId(externalUserId)
                    .withServiceAgreementId(internalServiceAgreementId)
                    .withFunctionGroupId(functionGroup.getFunctionAccessGroupId())
                    .withDataGroupIds(dataGroupIds))
                    .then()
                    .statusCode(SC_OK);

            LOGGER.info(String.format("Permission assigned for legal entity [%s], user [%s], service agreement [%s], function group [%s], data groups %s", externalLegalEntityId, externalUserId, internalServiceAgreementId == null ? "master" : internalServiceAgreementId, functionGroup.getFunctionAccessGroupId(), dataGroupIds));
        }
    }
}