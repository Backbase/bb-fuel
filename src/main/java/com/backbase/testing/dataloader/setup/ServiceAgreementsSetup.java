package com.backbase.testing.dataloader.setup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.PermissionsConfigurator;
import com.backbase.testing.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;

import java.io.IOException;
import java.util.Set;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();

    public void setupCustomServiceAgreements() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION), ServiceAgreementPostRequestBody[].class);

            for (ServiceAgreementPostRequestBody serviceAgreementGetResponseBody : serviceAgreementPostRequestBodies) {
                String serviceAgreementId = serviceAgreementsConfigurator.ingestServiceAgreementWithProvidersAndConsumersWithAllFunctionDataGroups(serviceAgreementGetResponseBody.getProviders(), serviceAgreementGetResponseBody.getConsumers());

                serviceAgreementGetResponseBody.getProviders().forEach(provider -> {
                    Set<String> externalUserIds = provider.getUsers();

                    externalUserIds.forEach(externalUserId -> serviceAgreementGetResponseBody.getConsumers().forEach(consumer -> {
                        String externalConsumerAdminUserId = consumer.getAdmins()
                                .iterator()
                                .next();

                        loginRestClient.login(USER_ADMIN, USER_ADMIN);
                        String externalLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalConsumerAdminUserId)
                                .then()
                                .statusCode(SC_OK)
                                .extract()
                                .as(LegalEntityByUserGetResponseBody.class)
                                .getExternalId();

                        permissionsConfigurator.assignAllFunctionDataGroupsOfLegalEntityToUserAndServiceAgreement(externalLegalEntityId, externalUserId, serviceAgreementId);
                    }));
                });
            }
        }
    }
}
