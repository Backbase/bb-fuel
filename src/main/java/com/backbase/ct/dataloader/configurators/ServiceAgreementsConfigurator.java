package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;
import static com.backbase.ct.dataloader.data.ServiceAgreementsDataGenerator.generateServiceAgreementPostRequestBody;
import static com.backbase.ct.dataloader.data.ServiceAgreementsDataGenerator.generateServiceAgreementPutRequestBody;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsConfigurator.class);

    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();
    private ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient = new ServiceAgreementsIntegrationRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();

    public String ingestServiceAgreementWithProvidersAndConsumers(Set<Participant> participants) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        enrichParticipantsWithExternalId(participants);

        String serviceAgreementId = serviceAgreementsIntegrationRestClient.ingestServiceAgreement(generateServiceAgreementPostRequestBody(participants))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(ServiceAgreementPostResponseBody.class)
                .getId();

        LOGGER.info("Service agreement ingested for participants {}", Arrays.toString(participants.toArray()));

        return serviceAgreementId;
    }

    public void updateMasterServiceAgreementWithExternalIdByLegalEntity(String internalLegalEntityId) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalServiceAgreementId = legalEntityPresentationRestClient.getMasterServiceAgreementOfLegalEntity(internalLegalEntityId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getId();

        serviceAgreementsIntegrationRestClient.updateServiceAgreement(internalServiceAgreementId, generateServiceAgreementPutRequestBody())
            .then()
            .statusCode(SC_OK);

        LOGGER.info("Service agreement [{}] updated with external id", internalServiceAgreementId);
    }

    private void enrichParticipantsWithExternalId(Set<Participant> participants) {
        for (Participant participant : participants) {
            String externalAdminUserId = participant.getAdmins()
                    .iterator()
                    .next();

            String externalLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalAdminUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class)
                .getExternalId();

            participant.setExternalId(externalLegalEntityId);
        }
    }
}
