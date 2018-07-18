package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.configurator.AccessGroupsConfigurator;
import com.backbase.ct.dataloader.configurator.PermissionsConfigurator;
import com.backbase.ct.dataloader.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.dto.CurrencyDataGroup;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.ct.dataloader.util.ParserUtil;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final PermissionsConfigurator permissionsConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final AccessControlSetup accessControlSetup;
    private String sepaFunctionGroupId;
    private String usWireFunctionGroupId;
    private String noSepaAndUsWireFunctionGroupId;
    private CurrencyDataGroup currencyDataGroup = null;


    public void setupCustomServiceAgreements() throws IOException {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil
                .convertJsonToObject(
                    this.globalProperties.getString(CommonConstants.PROPERTY_SERVICE_AGREEMENTS_JSON),
                    ServiceAgreementPostRequestBody[].class);

            this.loginRestClient.login(CommonConstants.USER_ADMIN, CommonConstants.USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            Arrays.stream(serviceAgreementPostRequestBodies)
                .forEach(serviceAgreementPostRequestBody -> {
                    String internalServiceAgreementId = this.serviceAgreementsConfigurator
                        .ingestServiceAgreementWithProvidersAndConsumers(
                            serviceAgreementPostRequestBody.getParticipants());

                    setupFunctionDataGroups(internalServiceAgreementId,
                        serviceAgreementPostRequestBody.getParticipants());
                    setupPermissions(internalServiceAgreementId, serviceAgreementPostRequestBody.getParticipants());
                });
        }
    }

    private void setupFunctionDataGroups(String internalServiceAgreementId, Set<Participant> participants) {
        Set<Participant> participantsSharingAccounts = participants.stream()
            .filter(Participant::getSharingAccounts)
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

        this.currencyDataGroup = this.accessControlSetup
            .ingestDataGroupArrangementsForServiceAgreement(externalServiceAgreementId, externalLegalEntityId);

        sepaFunctionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            singletonList(SEPA_CT_FUNCTION_NAME));

        usWireFunctionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            asList(US_DOMESTIC_WIRE_FUNCTION_NAME, US_FOREIGN_WIRE_FUNCTION_NAME));

        noSepaAndUsWireFunctionGroupId = this.accessGroupsConfigurator
            .ingestFunctionGroupsWithAllPrivilegesNotContainingProvidedFunctionNames(
                externalServiceAgreementId,
                asList(
                    SEPA_CT_FUNCTION_NAME,
                    US_DOMESTIC_WIRE_FUNCTION_NAME,
                    US_FOREIGN_WIRE_FUNCTION_NAME));
    }

    private void setupPermissions(String internalServiceAgreementId, Set<Participant> participants) {
        for (Participant participant : participants) {
            Set<String> externalUserIds = participant.getUsers();

            for (String externalUserId : externalUserIds) {
                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    internalServiceAgreementId,
                    this.sepaFunctionGroupId,
                    singletonList(currencyDataGroup.getInternalEurCurrencyDataGroupId()));

                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    internalServiceAgreementId,
                    this.usWireFunctionGroupId,
                    singletonList(currencyDataGroup.getInternalUsdCurrencyDataGroupId()));

                this.permissionsConfigurator.assignPermissions(
                    externalUserId,
                    internalServiceAgreementId,
                    noSepaAndUsWireFunctionGroupId,
                    asList(currencyDataGroup.getInternalRandomCurrencyDataGroupId(),
                        currencyDataGroup.getInternalEurCurrencyDataGroupId(),
                        currencyDataGroup.getInternalUsdCurrencyDataGroupId()));
            }
        }
    }
}
