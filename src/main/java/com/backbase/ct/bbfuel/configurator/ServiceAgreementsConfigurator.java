package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ServiceAgreementsDataGenerator.generateServiceAgreementPostRequestBody;
import static com.backbase.ct.bbfuel.data.ServiceAgreementsDataGenerator.generateServiceAgreementPutRequestBody;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IdItem;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.Participant;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.UserServiceAgreementPair;
import com.backbase.dbs.accesscontrol.client.v3.model.ServiceAgreementItem;
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

    public String ingestServiceAgreementWithProvidersAndConsumers(List<Participant> participants) {
        loginRestClient.loginBankAdmin();
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        enrichParticipantsWithExternalId(participants);

        String serviceAgreementId = serviceAgreementsIntegrationRestClient
            .ingestServiceAgreement(generateServiceAgreementPostRequestBody(participants))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(IdItem.class)
            .getId();

        if (log.isInfoEnabled()) {
            log.info("Service agreement ingested for participants {}", new ArrayList<>(participants));
        }

        return serviceAgreementId;
    }

    public void updateMasterServiceAgreementWithExternalIdByLegalEntity(String externalLegalEntityId) {
        String internalServiceAgreementId = legalEntityIntegrationRestClient
            .getMasterServiceAgreementOfLegalEntity(externalLegalEntityId)
            .getId();

        serviceAgreementsIntegrationRestClient
            .updateServiceAgreement(internalServiceAgreementId, generateServiceAgreementPutRequestBody())
            .then()
            .statusCode(SC_OK);

        log.info("Service agreement [{}] updated with external id", internalServiceAgreementId);
    }

    private void enrichParticipantsWithExternalId(List<Participant> participants) {
        for (Participant participant : participants) {
            String externalAdminUserId = participant.getAdmins()
                .iterator()
                .next();

            String externalLegalEntityId = userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(externalAdminUserId)
                .getExternalId();

            participant.setExternalId(externalLegalEntityId);
        }
    }

    public void setEntitlementsAdminUnderMsa(String user, String externalLeId) {
        ServiceAgreementItem msa = legalEntityIntegrationRestClient
            .getMasterServiceAgreementOfLegalEntity(externalLeId);
        serviceAgreementsIntegrationRestClient
            .addServiceAgreementAdminsBulk(Collections.singletonList(
                new UserServiceAgreementPair()
                    .externalUserId(user)
                    .externalServiceAgreementId(msa.getExternalId())));
    }
}
