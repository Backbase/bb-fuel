package com.backbase.ct.bbfuel.setup;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.configurator.AccessGroupsConfigurator;
import com.backbase.ct.bbfuel.configurator.PermissionsConfigurator;
import com.backbase.ct.bbfuel.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.dto.entitlement.DbsEntity;
import com.backbase.ct.bbfuel.service.ProductGroupService;
import com.backbase.ct.bbfuel.util.ParserUtil;
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
    private final ProductGroupService productGroupService;
    private String adminFunctionGroupId;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    @Override
    public void initiate() throws IOException {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil
                .convertJsonToObject(
                    this.globalProperties.getString(CommonConstants.PROPERTY_SERVICE_AGREEMENTS_JSON),
                    ServiceAgreementPostRequestBody[].class);
            ingestCustomServiceAgreements(asList(serviceAgreementPostRequestBodies));
        }
    }

    private void ingestCustomServiceAgreements(List<ServiceAgreementPostRequestBody> serviceAgreementPostRequestBodies) {
        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
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
            setupPermissions(internalServiceAgreementId, externalServiceAgreementId, serviceAgreementPostRequestBody.getParticipants());
        });
    }

    private void setupFunctionDataGroups(String internalServiceAgreementId, String externalServiceAgreementId,
        Set<Participant> participants) {
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

        this.accessControlSetup
            .ingestDataGroupArrangementsForServiceAgreement(internalServiceAgreementId, externalServiceAgreementId,
                externalLegalEntityId, users.size() == 1); //RB20180923: simplified assumption holds for now

        adminFunctionGroupId = this.accessGroupsConfigurator
            .ingestAdminFunctionGroup(externalServiceAgreementId).getId();
    }

    private void setupPermissions(String internalServiceAgreementId, String externalServiceAgreementId, Set<Participant> participants) {
        for (Participant participant : participants) {
            Set<String> externalUserIds = participant.getUsers();

            for (String externalUserId : externalUserIds) {
                List<String> dataGroupIds = this.productGroupService
                    .getAssignedProductGroups(externalServiceAgreementId)
                    .stream()
                    .map(DbsEntity::getId)
                    .collect(Collectors.toList());

                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    internalServiceAgreementId,
                    // TODO assess impact for different job profiles
                    this.adminFunctionGroupId,
                    dataGroupIds);
            }
        }
    }
}
