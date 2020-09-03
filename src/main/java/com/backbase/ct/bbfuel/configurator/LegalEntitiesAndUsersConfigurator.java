package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_IDENTITY_FEATURE_TOGGLE;
import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static com.backbase.ct.bbfuel.util.ResponseUtils.isConflictException;
import static com.backbase.ct.bbfuel.util.ResponseUtils.isNotFoundException;
import static org.apache.http.HttpStatus.SC_CREATED;


import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserIntegrationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.data.LegalEntitiesAndUsersDataGenerator;
import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;


import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.service.LegalEntityService;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.user.integration.rest.spec.v2.users.UsersPostRequestBody;


import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalEntitiesAndUsersConfigurator {

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final UserIntegrationRestClient userIntegrationRestClient;
    private final UserPresentationRestClient userPresentationRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final LegalEntityService legalEntityService;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;

    protected static GlobalProperties globalProperties = GlobalProperties.getInstance();

    /**
     * Dispatch the creation of legal entity depending whether given legalEntityWithUsers is a root entity.
     */
    public void ingestLegalEntityWithUsers(LegalEntityWithUsers legalEntityWithUsers) {
        if (legalEntityWithUsers.getCategory().isRoot()) {
            ingestRootLegalEntityAndEntitlementsAdmin(legalEntityWithUsers);
        } else {
            ingestLegalEntityAndUsers(legalEntityWithUsers);
        }
    }

    private void ingestRootLegalEntityAndEntitlementsAdmin(LegalEntityWithUsers root) {
        String externalLegalEntityId = this.legalEntityService
            .ingestLegalEntity(LegalEntitiesAndUsersDataGenerator
                .generateRootLegalEntitiesPostRequestBody(EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.serviceAgreementsConfigurator
            .updateMasterServiceAgreementWithExternalIdByLegalEntity(externalLegalEntityId);

        User admin = root.getUsers().get(0);
        this.userIntegrationRestClient.ingestAdminAndLogResponse(LegalEntitiesAndUsersDataGenerator
            .generateUsersPostRequestBody(admin, EXTERNAL_ROOT_LEGAL_ENTITY_ID));
        this.serviceAgreementsConfigurator
            .setEntitlementsAdminUnderMsa(admin.getExternalId(), EXTERNAL_ROOT_LEGAL_ENTITY_ID);
    }

    private void ingestLegalEntityAndUsers(LegalEntityWithUsers legalEntityWithUsers) {
        final LegalEntitiesPostRequestBody requestBody = LegalEntitiesAndUsersDataGenerator
            .composeLegalEntitiesPostRequestBody(
                legalEntityWithUsers.getLegalEntityExternalId(),
                legalEntityWithUsers.getLegalEntityName(),
                legalEntityWithUsers.getParentLegalEntityExternalId(),
                legalEntityWithUsers.getLegalEntityType());

        this.loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String externalLegalEntityId = this.legalEntityService.ingestLegalEntity(requestBody);

        legalEntityWithUsers.getUsers().parallelStream()
            .forEach(
                user -> this.ingestUserAndLogResponse(LegalEntitiesAndUsersDataGenerator
                    .generateUsersPostRequestBody(user, externalLegalEntityId)));
    }
    

    private void ingestUserAndLogResponse(UsersPostRequestBody user) {

        Response response;

        if (this.globalProperties.getBoolean(PROPERTY_IDENTITY_FEATURE_TOGGLE)) {
            response = this.userIntegrationRestClient.importUserIdentity(user);
        } else {
            response = this.userIntegrationRestClient.ingestUser(user);
        }

        if (isBadRequestException(response, "User already exists") || isConflictException(response, "User already exists")) {
            log.info("User [{}] already exists, skipped ingesting this user", user.getExternalId());
        } else if (isNotFoundException(response, "Identity does not exist in Identity Service")) {
            log.info("Identity for user [{}] not found, creating identity", user.getExternalId());

            String legalEntityId = legalEntityPresentationRestClient
                .retrieveLegalEntityByExternalId(user.getLegalEntityExternalId()).getId();

            com.backbase.dbs.user.presentation.rest.spec.v2.users.UsersPostRequestBody userBody =
                new com.backbase.dbs.user.presentation.rest.spec.v2.users.UsersPostRequestBody();

            userBody
                .withExternalId(user.getExternalId())
                .withFullName(user.getFullName())
                .withLegalEntityExternalId(user.getLegalEntityExternalId())
                .withPreferredLanguage(user.getPreferredLanguage());

            this.userPresentationRestClient.createIdentityUserAndLogResponse(userBody, legalEntityId);
        } else if (response.statusCode() == SC_CREATED) {
            log.info("User [{}] ingested under legal entity [{}]",
                user.getExternalId(), user.getLegalEntityExternalId());
        } else {
            log.info("User [{}] could not be ingested", user.getExternalId());
            response.then()
                .statusCode(SC_CREATED);
        }
    }
}
