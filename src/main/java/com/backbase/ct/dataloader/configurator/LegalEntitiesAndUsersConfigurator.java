package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.dataloader.enrich.LegalEntityWithUsersEnricher.createFakedAdminUser;

import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.user.UserIntegrationRestClient;
import com.backbase.ct.dataloader.data.LegalEntitiesAndUsersDataGenerator;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.service.LegalEntityService;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalEntitiesAndUsersConfigurator {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final UserIntegrationRestClient userIntegrationRestClient;
    private final LegalEntityService legalEntityService;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    public void ingestRootLegalEntityAndEntitlementsAdmin(String externalEntitlementsAdminUserId) {
        String externalLegalEntityId = this.legalEntityService.ingestLegalEntity(LegalEntitiesAndUsersDataGenerator
            .generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.serviceAgreementsConfigurator
            .updateMasterServiceAgreementWithExternalIdByLegalEntity(externalLegalEntityId);

        this.userIntegrationRestClient.ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
            .generateUsersPostRequestBody(
                createFakedAdminUser(externalEntitlementsAdminUserId), EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.userIntegrationRestClient.ingestEntitlementsAdminUnderLEAndLogResponse(externalEntitlementsAdminUserId,
            EXTERNAL_ROOT_LEGAL_ENTITY_ID);
    }

    public void ingestUsersUnderLegalEntity(LegalEntityWithUsers legalEntityWithUsers) {
        final LegalEntitiesPostRequestBody requestBody = LegalEntitiesAndUsersDataGenerator
            .composeLegalEntitiesPostRequestBody(
                legalEntityWithUsers.getLegalEntityExternalId(),
                legalEntityWithUsers.getLegalEntityName(),
                legalEntityWithUsers.getParentLegalEntityExternalId(),
                legalEntityWithUsers.getLegalEntityType());

        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String externalLegalEntityId = this.legalEntityService.ingestLegalEntity(requestBody);

        legalEntityWithUsers.getUsers().parallelStream()
            .forEach(
                user -> this.userIntegrationRestClient
                    .ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
                        .generateUsersPostRequestBody(user, externalLegalEntityId)));
    }
}
