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
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.ct.bbfuel.service.ProductGroupService;
import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.DataGroupNameIdentifier;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.FunctionGroupNameIdentifier;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.UserPermissionItem;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ParticipantCreateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementCreateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.User;
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
    private final LoginRestClient loginRestClient;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final PermissionsConfigurator permissionsConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final AccessControlSetup accessControlSetup;
    private final ProductGroupService productGroupService;

    @Override
    public void initiate() throws IOException {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementCreateRequest[] serviceAgreementPostRequestBodies = ParserUtil
                .convertJsonToObject(
                    this.globalProperties.getString(CommonConstants.PROPERTY_SERVICE_AGREEMENTS_JSON),
                    ServiceAgreementCreateRequest[].class);
            ingestCustomServiceAgreements(asList(serviceAgreementPostRequestBodies));
        }
    }

    private void ingestCustomServiceAgreements(List<ServiceAgreementCreateRequest> serviceAgreementPostRequestBodies) {
        this.loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        serviceAgreementPostRequestBodies.forEach(serviceAgreementPostRequestBody -> {
            String internalServiceAgreementId = this.serviceAgreementsConfigurator
                .ingestServiceAgreementWithProvidersAndConsumers(
                    serviceAgreementPostRequestBody.getParticipants());

            String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
                .retrieveServiceAgreement(internalServiceAgreementId)
                .getExternalId();

            JobProfile jobProfile = setupFunctionDataGroups(internalServiceAgreementId, externalServiceAgreementId,
                serviceAgreementPostRequestBody.getParticipants());
            setupPermissions(externalServiceAgreementId, serviceAgreementPostRequestBody.getParticipants(), jobProfile);
        });
    }

    private JobProfile setupFunctionDataGroups(String internalServiceAgreementId, String externalServiceAgreementId,
        List<ParticipantCreateRequest> participants) {
        List<ParticipantCreateRequest> participantsSharingAccounts = participants.stream()
            .filter(ParticipantCreateRequest::getSharingAccounts)
            .toList();

        Set<String> users = participants.stream()
            .map(ParticipantCreateRequest::getUsers)
            .flatMap(List::stream)
            .map(User::getExternalUserId)
            .collect(Collectors.toSet());

        String externalAdminUserId = participantsSharingAccounts.iterator()
            .next()
            .getAdmins()
            .iterator()
            .next()
            .getExternalUserId();

        String externalLegalEntityId = this.userPresentationRestClient
            .retrieveLegalEntityByExternalUserId(externalAdminUserId)
            .getExternalId();

        this.accessControlSetup
            .ingestDataGroupArrangementsForServiceAgreement(internalServiceAgreementId, externalServiceAgreementId,
                externalLegalEntityId, users.size() == 1); //RB20180923: simplified assumption holds for now

        return this.accessGroupsConfigurator.ingestAdminFunctionGroup(externalServiceAgreementId);
    }

    private void setupPermissions(String externalServiceAgreementId, List<ParticipantCreateRequest> participants,
        JobProfile jobProfile) {
        for (ParticipantCreateRequest participant : participants) {
            List<String> externalUserIds = participant.getUsers().stream()
                .map(User::getExternalUserId)
                .toList();

            for (String externalUserId : externalUserIds) {
                List<String> dataGroupNames = this.productGroupService
                    .findAssignedProductGroupsNames(externalServiceAgreementId);

                List<DataGroupNameIdentifier> dataGroupIdentifiers = dataGroupNames.stream()
                    .map(dataGroupName -> new DataGroupNameIdentifier()
                        .name(dataGroupName)
                        .dataGroupType("ARRANGEMENTS") //todo get rid of magic string
                        .serviceAgreementExternalId(externalServiceAgreementId))
                    .toList();

                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    externalServiceAgreementId,
                    // TODO assess impact for different job profiles
                    singletonList(new UserPermissionItem()
                        .functionGroup(new FunctionGroupNameIdentifier().name(jobProfile.getJobProfileName())
                            .serviceAgreementExternalId(jobProfile.getExternalServiceAgreementId()))
                        .dataGroups(dataGroupIdentifiers)));
            }
        }
    }
}
