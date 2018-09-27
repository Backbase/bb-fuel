package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static java.util.Arrays.asList;

import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.configurator.AccessGroupsConfigurator;
import com.backbase.ct.dataloader.configurator.PermissionsConfigurator;
import com.backbase.ct.dataloader.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.dto.DataGroupCollection;
import com.backbase.ct.dataloader.util.ParserUtil;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsSetup extends BaseSetup {

    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final PermissionsConfigurator permissionsConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final AccessControlSetup accessControlSetup;
    private String adminFunctionGroupId;
    private DataGroupCollection dataGroupCollection = null;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    @Override
    public void initiate() throws IOException {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil
                .convertJsonToObject(
                    this.globalProperties.getString(CommonConstants.PROPERTY_SERVICE_AGREEMENTS_JSON),
                    ServiceAgreementPostRequestBody[].class);
            ingestCustomSericeAgreements(asList(serviceAgreementPostRequestBodies));
        }
    }

    private void ingestCustomSericeAgreements(List<ServiceAgreementPostRequestBody> serviceAgreementPostRequestBodies) {
        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        serviceAgreementPostRequestBodies.forEach(serviceAgreementPostRequestBody -> {
            String internalServiceAgreementId = this.serviceAgreementsConfigurator
                .ingestServiceAgreementWithProvidersAndConsumers(
                    serviceAgreementPostRequestBody.getParticipants());

            setupFunctionDataGroups(internalServiceAgreementId,
                serviceAgreementPostRequestBody.getParticipants());
            setupPermissions(internalServiceAgreementId, serviceAgreementPostRequestBody.getParticipants());
        });
    }

    private void setupFunctionDataGroups(String internalServiceAgreementId, Set<Participant> participants) {
        Set<Participant> participantsSharingAccounts = participants.stream()
            .filter(Participant::getSharingAccounts)
            .collect(Collectors.toSet());

        Set<String> users = participants.stream()
            .map(Participant::getUsers)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        String externalAdminUserId = participantsSharingAccounts.iterator()
            .next()
            .getAdmins()
            .iterator()
            .next();

        String externalLegalEntityId = this.userPresentationRestClient
            .retrieveLegalEntityByExternalUserId(externalAdminUserId)
            .getExternalId();

        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();

        this.dataGroupCollection = this.accessControlSetup
            .ingestDataGroupArrangementsForServiceAgreement(externalServiceAgreementId, externalLegalEntityId,
                users.size() == 1); //RB20180923: simplified assumption holds for now

        adminFunctionGroupId = this.accessGroupsConfigurator
            .ingestAdminFunctionGroup(externalServiceAgreementId).getId();
    }

    private void setupPermissions(String internalServiceAgreementId, Set<Participant> participants) {
        for (Participant participant : participants) {
            Set<String> externalUserIds = participant.getUsers();

            for (String externalUserId : externalUserIds) {
                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    internalServiceAgreementId,
                    // TODO assess impact for different job profiles
                    this.adminFunctionGroupId,
                    dataGroupCollection.getDataGroupIds());
            }
        }
    }
}
