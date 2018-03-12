package com.backbase.testing.dataloader.setup;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    private Map<String, String> functionGroupFunctionNames = new HashMap<>();
    private CurrencyDataGroup currencyDataGroup = null;
    private FunctionsGetResponseBody[] functions;
    private LegalEntitiesWithUsersAssembler legalEntitiesWithUsersAssembler = new LegalEntitiesWithUsersAssembler();

    public ServiceAgreementsSetup() throws IOException {
    }

    public void setupCustomServiceAgreements() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS)) {
            ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil
                .convertJsonToObject(this.globalProperties.getString(PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION), ServiceAgreementPostRequestBody[].class);
            this.functions = this.accessGroupIntegrationRestClient.retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class);

            this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            Arrays.stream(serviceAgreementPostRequestBodies)
                .forEach(serviceAgreementPostRequestBody -> {
                    String internalServiceAgreementId = this.serviceAgreementsConfigurator
                        .ingestServiceAgreementWithProvidersAndConsumers(serviceAgreementPostRequestBody.getProviders(),
                            serviceAgreementPostRequestBody.getConsumers());

                    setupFunctionDataGroups(internalServiceAgreementId, serviceAgreementPostRequestBody.getConsumers());
                    setupPermissions(internalServiceAgreementId, serviceAgreementPostRequestBody.getProviders());

                    this.functionGroupFunctionNames.clear();
                });
        }
    }

    private void setupFunctionDataGroups(String internalServiceAgreementId, Set<Consumer> consumers) {
        String externalConsumerAdminUserId = consumers.iterator().next().getAdmins()
            .iterator()
            .next();

        String externalLegalEntityId = this.userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalConsumerAdminUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByUserGetResponseBody.class)
            .getExternalId();

        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient.retrieveServiceAgreement(internalServiceAgreementId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getExternalId();

        this.currencyDataGroup = this.legalEntitiesWithUsersAssembler
            .getDataGroupArrangementsForServiceAgreement(externalServiceAgreementId, externalLegalEntityId);

        for (FunctionsGetResponseBody function : this.functions) {
            String functionName = function.getName();

            String functionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupWithAllPrivilegesByFunctionName(externalServiceAgreementId, functionName);

            this.functionGroupFunctionNames.put(functionGroupId, functionName);
        }
    }

    private void setupPermissions(String internalServiceAgreementId, Set<Provider> providers) {
        for (Provider provider : providers) {
            Set<String> externalUserIds = provider.getUsers();

            for (String externalUserId : externalUserIds) {
                for (Map.Entry<String, String> entry : this.functionGroupFunctionNames.entrySet()) {
                    String functionGroupId = entry.getKey();
                    String functionName = entry.getValue();

                    this.permissionsConfigurator
                        .assignPermissions(externalUserId, internalServiceAgreementId, functionName, functionGroupId, this.currencyDataGroup);
                }
            }
        }
    }
}