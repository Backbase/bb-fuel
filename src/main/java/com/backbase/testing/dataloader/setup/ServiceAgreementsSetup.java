package com.backbase.testing.dataloader.setup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.testing.dataloader.dto.CurrencyDataGroup;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsSetup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsSetup.class);

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    private ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient = new ServiceAgreementsPresentationRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private UsersSetup usersSetup = new UsersSetup();

    public ServiceAgreementsSetup() throws IOException {
    }

    public void setupCustomServiceAgreements() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION), ServiceAgreementPostRequestBody[].class);

            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            Arrays.stream(serviceAgreementPostRequestBodies).forEach(serviceAgreementPostRequestBody -> {
                LOGGER.info("ingesting serviceAgreementPostRequestBody with name [{}]", serviceAgreementPostRequestBody.getName());
                String internalServiceAgreementId = serviceAgreementsConfigurator.ingestServiceAgreementWithProvidersAndConsumers(serviceAgreementPostRequestBody.getProviders(), serviceAgreementPostRequestBody.getConsumers());

                serviceAgreementPostRequestBody.getProviders().forEach(provider -> {
                    Set<String> externalUserIds = provider.getUsers();

                    externalUserIds.parallelStream().forEach(externalUserId -> serviceAgreementPostRequestBody.getConsumers().forEach(consumer -> {
                        String externalConsumerAdminUserId = consumer.getAdmins()
                                .iterator()
                                .next();

                        String externalLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalConsumerAdminUserId)
                                .then()
                                .statusCode(SC_OK)
                                .extract()
                                .as(LegalEntityByUserGetResponseBody.class)
                                .getExternalId();

                        String externalServiceAgreementId = serviceAgreementsPresentationRestClient.retrieveServiceAgreement(internalServiceAgreementId)
                                .then()
                                .statusCode(SC_OK)
                                .extract()
                                .as(ServiceAgreementGetResponseBody.class)
                                .getExternalId();

                        CurrencyDataGroup currencyDataGroup = usersSetup.setupArrangementsPerDataGroupForServiceAgreement(externalServiceAgreementId, externalLegalEntityId);
                        usersSetup.setupFunctionGroupsAndAssignPermissions(externalUserId, internalServiceAgreementId, externalServiceAgreementId, currencyDataGroup, false);
                    }));
                });
            });
        }
    }
}
