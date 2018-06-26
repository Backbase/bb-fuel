package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.dataloader.clients.user.UserIntegrationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.LegalEntitiesAndUsersDataGenerator;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalEntitiesAndUsersConfigurator {

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

    public void ingestUsersUnderLegalEntity(List<String> externalUserIds, String parentLegalEntityExternalId,
        String legalEntityExternalId,
        String legalEntityName, String type) {
        final LegalEntitiesPostRequestBody requestBody = LegalEntitiesAndUsersDataGenerator
            .composeLegalEntitiesPostRequestBody(legalEntityExternalId, legalEntityName,
                parentLegalEntityExternalId, type);

        this.legalEntityIntegrationRestClient.ingestLegalEntityAndLogResponse(requestBody);

        externalUserIds.parallelStream()
            .forEach(
                externalUserId -> this.userIntegrationRestClient
                    .ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
                        .generateUsersPostRequestBody(externalUserId, requestBody.getExternalId())));
    }
}
