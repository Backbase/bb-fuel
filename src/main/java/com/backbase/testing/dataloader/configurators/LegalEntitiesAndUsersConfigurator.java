package com.backbase.testing.dataloader.configurators;

import com.backbase.testing.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserIntegrationRestClient;
import com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateExternalLegalEntityId;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateRootLegalEntitiesPostRequestBody;
import static com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator.generateUsersPostRequestBody;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class LegalEntitiesAndUsersConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntitiesAndUsersConfigurator.class);

    private LegalEntityIntegrationRestClient legalEntityIntegrationRestClient = new LegalEntityIntegrationRestClient();
    private UserIntegrationRestClient userIntegrationRestClient = new UserIntegrationRestClient();

    public void ingestRootLegalEntityAndEntitlementsAdmin(String rootLegalEntityId, String externalEntitlementsAdminUserId) {
        legalEntityIntegrationRestClient.ingestLegalEntitySkipIfAlreadyExists(generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));

        userIntegrationRestClient.ingestUserSkipIfAlreadyExists(generateUsersPostRequestBody(externalEntitlementsAdminUserId, rootLegalEntityId));

        userIntegrationRestClient.ingestEntitlementsAdminUnderLESkipIfAlreadyExists(externalEntitlementsAdminUserId, rootLegalEntityId);
    }

    public void ingestUsersUnderNewLegalEntity(List<String> externalUserIds, String externalParentLegalEntityId) {
        String externalLegalEntityId = generateExternalLegalEntityId();

        legalEntityIntegrationRestClient.ingestLegalEntitySkipIfAlreadyExists(generateLegalEntitiesPostRequestBody(externalLegalEntityId, externalParentLegalEntityId));

        externalUserIds.parallelStream().forEach(externalUserId -> {
            userIntegrationRestClient.ingestUserSkipIfAlreadyExists(generateUsersPostRequestBody(externalUserId, externalLegalEntityId));
        });
    }
}