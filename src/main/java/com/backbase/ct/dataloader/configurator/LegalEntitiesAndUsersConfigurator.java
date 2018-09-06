package com.backbase.ct.dataloader.configurator;

import com.backbase.ct.dataloader.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.dataloader.client.user.UserIntegrationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.LegalEntitiesAndUsersDataGenerator;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalEntitiesAndUsersConfigurator {

    private Faker faker = new Faker();
    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;
    private final UserIntegrationRestClient userIntegrationRestClient;

    public void ingestRootLegalEntityAndEntitlementsAdmin(String externalEntitlementsAdminUserId) {
        this.legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(LegalEntitiesAndUsersDataGenerator
            .generateRootLegalEntitiesPostRequestBody(CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
            .generateUsersPostRequestBody(externalEntitlementsAdminUserId,
                CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestEntitlementsAdminUnderLEAndLogResponse(externalEntitlementsAdminUserId,
            CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID);
    }

    public void ingestUsersUnderLegalEntity(LegalEntityWithUsers legalEntityWithUsers) {
        String legalEntityName = legalEntityWithUsers.getUserExternalIds().size() == 1 &&
            legalEntityWithUsers.getLegalEntityName() == null
            ? faker.name().firstName() + " " + faker.name().lastName()
            : legalEntityWithUsers.getLegalEntityName();

        final LegalEntitiesPostRequestBody requestBody = LegalEntitiesAndUsersDataGenerator
            .composeLegalEntitiesPostRequestBody(
                legalEntityWithUsers.getLegalEntityExternalId(),
                legalEntityName,
                legalEntityWithUsers.getParentLegalEntityExternalId(),
                legalEntityWithUsers.getLegalEntityType());

        this.legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(requestBody);

        legalEntityWithUsers.getUserExternalIds().parallelStream()
            .forEach(
                externalUserId -> this.userIntegrationRestClient
                    .ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
                        .generateUsersPostRequestBody(externalUserId, requestBody.getExternalId())));
    }
}
