package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;

import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.dataloader.client.user.UserIntegrationRestClient;
import com.backbase.ct.dataloader.data.LegalEntitiesAndUsersDataGenerator;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.service.LegalEntityService;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalEntitiesAndUsersConfigurator {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private Faker faker = new Faker();
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;
    private final UserIntegrationRestClient userIntegrationRestClient;
    private final LegalEntityService legalEntityService;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    public void ingestRootLegalEntityAndEntitlementsAdmin(String externalEntitlementsAdminUserId) {
        this.legalEntityService.ingestLegalEntity(LegalEntitiesAndUsersDataGenerator
            .generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
            .generateUsersPostRequestBody(externalEntitlementsAdminUserId, EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestEntitlementsAdminUnderLEAndLogResponse(externalEntitlementsAdminUserId,
            EXTERNAL_ROOT_LEGAL_ENTITY_ID);
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

        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        this.legalEntityService.ingestLegalEntity(requestBody);

        legalEntityWithUsers.getUserExternalIds().parallelStream()
            .forEach(
                externalUserId -> this.userIntegrationRestClient
                    .ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
                        .generateUsersPostRequestBody(externalUserId, requestBody.getExternalId())));
    }
}
