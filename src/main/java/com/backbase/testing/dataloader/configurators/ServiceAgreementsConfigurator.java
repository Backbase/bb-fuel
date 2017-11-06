package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.FunctionDataGroupPair;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.data.ServiceAgreementsDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsConfigurator.class);

    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient = new ServiceAgreementsIntegrationRestClient();
    private ServiceAgreementsDataGenerator serviceAgreementsDataGenerator = new ServiceAgreementsDataGenerator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();

    public void ingestServiceAgreementWithProvidersAndConsumersWithAllFunctionDataGroups(Set<Provider> providers, Set<Consumer> consumers) {
        Set<FunctionDataGroupPair> functionDataGroupPairs = new HashSet<>();

        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        for (Consumer consumer : consumers) {
            String externalConsumerAdminUserId = consumer.getAdmins()
                    .iterator()
                    .next();

            LegalEntityByUserGetResponseBody legalEntity = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalConsumerAdminUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class);

            String externalConsumerLegalEntityId = legalEntity.getExternalId();
            String internalConsumerLegalEntityId = legalEntity.getId();

            FunctionAccessGroupsGetResponseBody[] functionGroups = accessGroupPresentationRestClient.retrieveFunctionGroupsByLegalEntity(internalConsumerLegalEntityId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(FunctionAccessGroupsGetResponseBody[].class);

            Set<String> dataGroupIds = new HashSet<>(accessGroupPresentationRestClient.retrieveAllDataGroupIdsByLegalEntity(internalConsumerLegalEntityId));

            for (FunctionAccessGroupsGetResponseBody functionGroup : functionGroups) {
                functionDataGroupPairs.add(new FunctionDataGroupPair()
                        .withFunctionGroup(functionGroup.getFunctionAccessGroupId())
                        .withDataGroup(dataGroupIds));
            }

            consumer.withId(externalConsumerLegalEntityId)
                    .withFunctionDataGroupPairs(functionDataGroupPairs);
        }

        for (Provider provider : providers) {
            String externalProviderAdminUserId = provider.getAdmins()
                    .iterator()
                    .next();

            String externalProviderLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalProviderAdminUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class)
                    .getExternalId();

            provider.setId(externalProviderLegalEntityId);
        }

        serviceAgreementsIntegrationRestClient.ingestServiceAgreement(serviceAgreementsDataGenerator.generateServiceAgreementPostRequestBody(providers, consumers));
        LOGGER.info(String.format("Service agreement ingested for provider legal entities - admins/users %s, consumer legal entities - admins with all function groups and data groups exposed %s", Arrays.toString(providers.toArray()), Arrays.toString(consumers.toArray())));
    }
}
