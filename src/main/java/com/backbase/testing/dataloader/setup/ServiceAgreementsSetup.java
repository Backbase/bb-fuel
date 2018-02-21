package com.backbase.testing.dataloader.setup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.testing.dataloader.configurators.PermissionsConfigurator;
import com.backbase.testing.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.testing.dataloader.dto.CurrencyDataGroup;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    private ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient = new ServiceAgreementsPresentationRestClient();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    private PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private UsersSetup usersSetup = new UsersSetup();
    private Map<String, String> functionGroupFunctionNames = new HashMap<>();
    private CurrencyDataGroup currencyDataGroup = null;
    private FunctionsGetResponseBody[] functions;

    public ServiceAgreementsSetup() throws IOException {
    }

    public void setupCustomServiceAgreements() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION), ServiceAgreementPostRequestBody[].class);
            functions = accessGroupIntegrationRestClient.retrieveFunctions()
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(FunctionsGetResponseBody[].class);

            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            Arrays.stream(serviceAgreementPostRequestBodies)
                    .forEach(serviceAgreementPostRequestBody -> {
                        String internalServiceAgreementId = serviceAgreementsConfigurator.ingestServiceAgreementWithProvidersAndConsumers(serviceAgreementPostRequestBody.getProviders(), serviceAgreementPostRequestBody.getConsumers());

                        setupConsumers(internalServiceAgreementId, serviceAgreementPostRequestBody.getConsumers());
                        setupProviders(internalServiceAgreementId, serviceAgreementPostRequestBody.getProviders());
                    });
        }
    }

    private void setupConsumers(String internalServiceAgreementId, Set<Consumer> consumers) {
        for (Consumer consumer : consumers) {
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

            currencyDataGroup = usersSetup.setupArrangementsPerDataGroupForServiceAgreement(externalServiceAgreementId, externalLegalEntityId);

            for (FunctionsGetResponseBody function : functions) {
                String functionName = function.getName();

                String functionGroupId = accessGroupsConfigurator.ingestFunctionGroupWithAllPrivilegesByFunctionName(externalServiceAgreementId, functionName);

                functionGroupFunctionNames.put(functionGroupId, functionName);
            }
        }
    }

    private void setupProviders(String internalServiceAgreementId, Set<Provider> providers) {
        for (Provider provider : providers) {
            Set<String> externalUserIds = provider.getUsers();

            for (String externalUserId : externalUserIds) {
                for (Map.Entry<String, String> entry : functionGroupFunctionNames.entrySet()) {
                    String functionGroupId = entry.getKey();
                    String functionName = entry.getValue();

                    permissionsConfigurator.assignPermissions(externalUserId, internalServiceAgreementId, functionName, functionGroupId, currencyDataGroup);
                }
            }
        }
    }
}
