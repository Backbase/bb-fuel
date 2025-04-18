package com.backbase.ct.bbfuel.setup;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.configurator.AccessGroupsConfigurator;
import com.backbase.ct.bbfuel.configurator.PermissionsConfigurator;
import com.backbase.ct.bbfuel.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.service.ProductGroupService;
import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.*;
import java.io.IOException;
import java.util.ArrayList;
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
    private final LoginRestClient loginRestClient;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final PermissionsConfigurator permissionsConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final AccessControlSetup accessControlSetup;
    private final ProductGroupService productGroupService;
    private String adminFunctionGroupId;

    @Override
    public void initiate() throws IOException {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreement[] serviceAgreementPostRequestBodies = ParserUtil
                .convertJsonToObject(
                    this.globalProperties.getString(CommonConstants.PROPERTY_SERVICE_AGREEMENTS_JSON),
                    ServiceAgreement[].class);
            ingestCustomServiceAgreements(asList(serviceAgreementPostRequestBodies));
        }
    }

    private void ingestCustomServiceAgreements(List<ServiceAgreement> serviceAgreementPostRequestBodies) {
        this.loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        serviceAgreementPostRequestBodies.forEach(serviceAgreementPostRequestBody -> {
            String internalServiceAgreementId = this.serviceAgreementsConfigurator
                .ingestServiceAgreementWithProvidersAndConsumers(
                    serviceAgreementPostRequestBody.getParticipants());

            String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
                .retrieveServiceAgreement(internalServiceAgreementId)
                .getExternalId();

            setupFunctionDataGroups(internalServiceAgreementId, externalServiceAgreementId,
                serviceAgreementPostRequestBody.getParticipants());
            setupPermissions(externalServiceAgreementId, serviceAgreementPostRequestBody.getParticipants());
        });
    }

    private void setupFunctionDataGroups(String internalServiceAgreementId, String externalServiceAgreementId,
        List<Participant> participants) {
        List<Participant> participantsSharingAccounts = participants.stream()
            .filter(Participant::getSharingAccounts)
            .toList();

        Set<String> users = participants.stream()
            .map(Participant::getUsers)
            .flatMap(List::stream)
            .collect(Collectors.toSet());

        String externalAdminUserId = participantsSharingAccounts.iterator()
            .next()
            .getAdmins()
            .iterator()
            .next();

        String externalLegalEntityId = this.userPresentationRestClient
            .retrieveLegalEntityByExternalUserId(externalAdminUserId)
            .getExternalId();

        this.accessControlSetup
            .ingestDataGroupArrangementsForServiceAgreement(internalServiceAgreementId, externalServiceAgreementId,
                externalLegalEntityId, users.size() == 1); //RB20180923: simplified assumption holds for now

        adminFunctionGroupId = this.accessGroupsConfigurator
            .ingestAdminFunctionGroup(externalServiceAgreementId).getId();
    }

    private void setupPermissions(String externalServiceAgreementId, List<Participant> participants) {
        for (Participant participant : participants) {
            List<String> externalUserIds = participant.getUsers();

            for (String externalUserId : externalUserIds) {
                List<String> dataGroupIds = this.productGroupService
                    .findAssignedProductGroupsIds(externalServiceAgreementId);

                List<IntegrationDataGroupIdentifier> dataGroupIdentifiers = new ArrayList<>();
                dataGroupIds.forEach(dataGroupId -> dataGroupIdentifiers.add(new IntegrationDataGroupIdentifier().idIdentifier(dataGroupId)));

                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    externalServiceAgreementId,
                    // TODO assess impact for different job profiles
                    singletonList(new IntegrationFunctionGroupDataGroup()
                        .functionGroupIdentifier(
                            new IntegrationIdentifier().idIdentifier(this.adminFunctionGroupId))
                        .dataGroupIdentifiers(dataGroupIdentifiers)));
            }
        }
    }
}
