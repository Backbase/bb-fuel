package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ServiceAgreementsDataGenerator.generateServiceAgreementPostRequestBody;
import static com.backbase.ct.bbfuel.data.ServiceAgreementsDataGenerator.generateServiceAgreementPutRequestBody;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.dbs.accesscontrol.ac_legalentity.integration.v3.model.SingleServiceAgreement;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ParticipantCreateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ResultId;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementAdmin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAgreementsConfigurator {

    private final LoginRestClient loginRestClient;
    private final UserPresentationRestClient userPresentationRestClient;
    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;
    private final ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;

    public String ingestServiceAgreementWithProvidersAndConsumers(List<ParticipantCreateRequest> participants) {
        loginRestClient.loginBankAdmin();
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        enrichParticipantsWithExternalId(participants);

        String serviceAgreementId = serviceAgreementsIntegrationRestClient
            .ingestServiceAgreement(generateServiceAgreementPostRequestBody(participants))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(ResultId.class)
            .getId();

        if (log.isInfoEnabled()) {
            log.info("Service agreement ingested for participants {}", new ArrayList<>(participants));
        }

        return serviceAgreementId;
    }

    public void updateMasterServiceAgreementWithExternalIdByLegalEntity(String externalLegalEntityId) {
        String serviceAgreementId = legalEntityIntegrationRestClient
            .getSingleServiceAgreementOfLegalEntity(externalLegalEntityId)
            .getId();

        serviceAgreementsIntegrationRestClient
            .updateServiceAgreement(serviceAgreementId, generateServiceAgreementPutRequestBody())
            .then()
            .statusCode(SC_NO_CONTENT);

        log.info("Service agreement [{}] updated with external id", serviceAgreementId);
    }

    private void enrichParticipantsWithExternalId(List<ParticipantCreateRequest> participants) {
        for (ParticipantCreateRequest participant : participants) {
            String externalAdminUserId = participant.getAdmins()
                .iterator()
                .next()
                .getExternalUserId();

            String externalLegalEntityId = userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(externalAdminUserId)
                .getExternalId();

            participant.setExternalId(externalLegalEntityId);
        }
    }

    public void setEntitlementsAdminUnderMsa(String user, String externalLeId) {
        SingleServiceAgreement msa = legalEntityIntegrationRestClient
            .getSingleServiceAgreementOfLegalEntity(externalLeId);
        serviceAgreementsIntegrationRestClient
            .addServiceAgreementAdminsBulk(Collections.singletonList(
                new ServiceAgreementAdmin()
                    .externalUserId(user)
                    .externalServiceAgreementId(msa.getExternalId())));
    }
}
