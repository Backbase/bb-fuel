package com.backbase.testing.dataloader.configurators;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.composeLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateRootLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateUsersPostRequestBody;

import com.backbase.testing.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserIntegrationRestClient;
import java.util.List;

public class LegalEntitiesAndUsersConfigurator {

    private LegalEntityIntegrationRestClient legalEntityIntegrationRestClient = new LegalEntityIntegrationRestClient();
    private UserIntegrationRestClient userIntegrationRestClient = new UserIntegrationRestClient();

    public void ingestRootLegalEntityAndEntitlementsAdmin(String externalEntitlementsAdminUserId) {
        this.legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestUserAndLogResponse(generateUsersPostRequestBody(externalEntitlementsAdminUserId, EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestEntitlementsAdminUnderLEAndLogResponse(externalEntitlementsAdminUserId, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
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