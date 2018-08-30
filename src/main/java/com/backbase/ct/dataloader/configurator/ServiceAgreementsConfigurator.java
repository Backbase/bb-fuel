package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.dataloader.data.ServiceAgreementsDataGenerator.generateServiceAgreementPostRequestBody;
import static com.backbase.ct.dataloader.data.ServiceAgreementsDataGenerator.generateServiceAgreementPutRequestBody;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import java.util.Arrays;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceAgreementsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsConfigurator.class);
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserPresentationRestClient userPresentationRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    public String ingestServiceAgreementWithProvidersAndConsumers(Set<Participant> participants) {
        loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        enrichParticipantsWithExternalId(participants);

        String serviceAgreementId = serviceAgreementsIntegrationRestClient
            .ingestServiceAgreement(generateServiceAgreementPostRequestBody(participants))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(ServiceAgreementPostResponseBody.class)
            .getId();

        LOGGER.info("Service agreement ingested for participants {}", Arrays.toString(participants.toArray()));

        return serviceAgreementId;
    }

    public void updateMasterServiceAgreementWithExternalIdByLegalEntity(String internalLegalEntityId) {
        loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalServiceAgreementId = legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(internalLegalEntityId)
            .getId();

        serviceAgreementsIntegrationRestClient
            .updateServiceAgreement(internalServiceAgreementId, generateServiceAgreementPutRequestBody())
            .then()
            .statusCode(SC_OK);

        LOGGER.info("Service agreement [{}] updated with external id", internalServiceAgreementId);
    }

    private void enrichParticipantsWithExternalId(Set<Participant> participants) {
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
}
