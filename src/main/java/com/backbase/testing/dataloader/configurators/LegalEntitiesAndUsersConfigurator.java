package com.backbase.testing.dataloader.configurators;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.composeLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateExternalLegalEntityId;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateRootLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateUsersPostRequestBody;

import com.backbase.testing.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserIntegrationRestClient;
import java.util.List;

public class LegalEntitiesAndUsersConfigurator {

    private LegalEntityIntegrationRestClient legalEntityIntegrationRestClient = new LegalEntityIntegrationRestClient();
    private UserIntegrationRestClient userIntegrationRestClient = new UserIntegrationRestClient();

    public void ingestRootLegalEntityAndEntitlementsAdmin(String rootLegalEntityId, String externalEntitlementsAdminUserId) {
        this.legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestUserAndLogResponse(generateUsersPostRequestBody(externalEntitlementsAdminUserId, rootLegalEntityId));
        this.userIntegrationRestClient.ingestEntitlementsAdminUnderLEAndLogResponse(externalEntitlementsAdminUserId, rootLegalEntityId);
    }

    @Deprecated
    public void ingestUsersUnderNewLegalEntity(List<String> externalUserIds, String externalParentLegalEntityId) {
        String externalLegalEntityId = generateExternalLegalEntityId();
        this.legalEntityIntegrationRestClient
            .ingestLegalEntityAndLogResponse(generateLegalEntitiesPostRequestBody(externalLegalEntityId, externalParentLegalEntityId));
        externalUserIds.parallelStream()
            .forEach(
                externalUserId -> this.userIntegrationRestClient.ingestUserAndLogResponse(generateUsersPostRequestBody(externalUserId, externalLegalEntityId)));
    }

    public void ingestUsersUnderComposedLegalEntity(List<String> externalUserIds, String parentLegalEntityExternalId, String legalEntityExternalId,
        String legalEntityName, String type) {
        this.legalEntityIntegrationRestClient
            .ingestLegalEntityAndLogResponse(composeLegalEntitiesPostRequestBody(legalEntityExternalId, legalEntityName, parentLegalEntityExternalId, type));
        externalUserIds.parallelStream()
            .forEach(
                externalUserId -> this.userIntegrationRestClient.ingestUserAndLogResponse(generateUsersPostRequestBody(externalUserId, legalEntityExternalId)));
    }
}