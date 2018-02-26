package com.backbase.testing.dataloader.configurators;

import com.backbase.testing.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserIntegrationRestClient;

import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateExternalLegalEntityId;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateRootLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateUsersPostRequestBody;

public class LegalEntitiesAndUsersConfigurator {

    private LegalEntityIntegrationRestClient legalEntityIntegrationRestClient = new LegalEntityIntegrationRestClient();
    private UserIntegrationRestClient userIntegrationRestClient = new UserIntegrationRestClient();

    public void ingestRootLegalEntityAndEntitlementsAdmin(String rootLegalEntityId, String externalEntitlementsAdminUserId) {
        legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));

        userIntegrationRestClient.ingestUserAndLogResponse(generateUsersPostRequestBody(externalEntitlementsAdminUserId, rootLegalEntityId));

        userIntegrationRestClient.ingestEntitlementsAdminUnderLESkipIfAlreadyExists(externalEntitlementsAdminUserId, rootLegalEntityId);
    }

    public void ingestUsersUnderNewLegalEntity(List<String> externalUserIds, String externalParentLegalEntityId) {
        String externalLegalEntityId = generateExternalLegalEntityId();

        legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(generateLegalEntitiesPostRequestBody(externalLegalEntityId, externalParentLegalEntityId));

        externalUserIds.parallelStream().forEach(externalUserId -> {
            userIntegrationRestClient.ingestUserAndLogResponse(generateUsersPostRequestBody(externalUserId, externalLegalEntityId));
        });
    }
}