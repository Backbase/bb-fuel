package com.backbase.testing.dataloader.configurators;

import com.backbase.testing.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserIntegrationRestClient;
import com.backbase.testing.dataloader.data.LegalEntitiesAndUsersDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class LegalEntitiesAndUsersConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntitiesAndUsersConfigurator.class);

    private LegalEntitiesAndUsersDataGenerator entitlementsDataGenerator = new LegalEntitiesAndUsersDataGenerator();
    private LegalEntityIntegrationRestClient legalEntityIntegrationRestClient = new LegalEntityIntegrationRestClient();
    private UserIntegrationRestClient userIntegrationRestClient = new UserIntegrationRestClient();

    public void ingestRootLegalEntityAndEntitlementsAdmin(String rootLegalEntityId, String externalEntitlementsAdminUserId) {
        legalEntityIntegrationRestClient.ingestLegalEntity(entitlementsDataGenerator.generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID))
                .then()
                .statusCode(SC_CREATED);

        LOGGER.info(String.format("Root legal entity [%s] ingested", EXTERNAL_ROOT_LEGAL_ENTITY_ID));

        userIntegrationRestClient.ingestUser(entitlementsDataGenerator.generateUsersPostRequestBody(externalEntitlementsAdminUserId, rootLegalEntityId))
                .then()
                .statusCode(SC_CREATED);

        LOGGER.info(String.format("User [%s] ingested under legal entity [%s]", externalEntitlementsAdminUserId, EXTERNAL_ROOT_LEGAL_ENTITY_ID));

        userIntegrationRestClient.ingestEntitlementsAdminUnderLE(externalEntitlementsAdminUserId, rootLegalEntityId)
                .then()
                .statusCode(SC_OK);

        LOGGER.info(String.format("Entitlements admin [%s] ingested under legal entity [%s]", externalEntitlementsAdminUserId, EXTERNAL_ROOT_LEGAL_ENTITY_ID));
    }

    public void ingestUsersUnderNewLegalEntity(List<String> externalUserIds, String externalParentLegalEntityId) {
        String externalLegalEntityId = entitlementsDataGenerator.generateExternalLegalEntityId();

        legalEntityIntegrationRestClient.ingestLegalEntity(entitlementsDataGenerator.generateLegalEntitiesPostRequestBody(externalLegalEntityId, externalParentLegalEntityId))
                .then()
                .statusCode(SC_CREATED);

        LOGGER.info(String.format("Legal entity [%s] ingested under legal entity [%s]", externalLegalEntityId, externalParentLegalEntityId));

        externalUserIds.parallelStream().forEach(externalUserId -> {
            userIntegrationRestClient.ingestUser(entitlementsDataGenerator.generateUsersPostRequestBody(externalUserId, externalLegalEntityId))
                    .then()
                    .statusCode(SC_CREATED);

            LOGGER.info(String.format("User [%s] ingested under legal entity [%s]", externalUserId, externalLegalEntityId));
        });
    }
}