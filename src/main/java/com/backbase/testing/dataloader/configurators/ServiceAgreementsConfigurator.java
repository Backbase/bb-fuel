package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.FunctionDataGroupPair;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.testing.dataloader.data.ServiceAgreementsDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsConfigurator.class);

    private ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient = new ServiceAgreementsIntegrationRestClient();
    private ServiceAgreementsDataGenerator serviceAgreementsDataGenerator = new ServiceAgreementsDataGenerator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();

    public void ingestServiceAgreementWithOneProviderOneConsumerAndUsersWithAllPrivileges(String externalProviderLegalEntityId, Set<String> externalProviderUserIds, String externalConsumerLegalEntityId, Set<String> externalConsumerUserIds) {
        Set<Provider> providers = new HashSet<>();
        Set<Consumer> consumers = new HashSet<>();
        Set<FunctionDataGroupPair> functionDataGroupPairs = new HashSet<>();

        String internalLegalEntityId = legalEntityPresentationRestClient.retrieveLegalEntityByExternalId(externalConsumerLegalEntityId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(LegalEntityByExternalIdGetResponseBody.class)
                .getId();

        FunctionAccessGroupsGetResponseBody[] functionGroups = accessGroupPresentationRestClient.retrieveFunctionGroupsByLegalEntity(internalLegalEntityId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionAccessGroupsGetResponseBody[].class);

        Set<String> dataGroupIds = new HashSet<>(accessGroupPresentationRestClient.retrieveAllDataGroupIdsByLegalEntity(internalLegalEntityId));

        for (FunctionAccessGroupsGetResponseBody functionGroup : functionGroups) {
            functionDataGroupPairs.add(new FunctionDataGroupPair()
                    .withFunctionGroup(functionGroup.getFunctionAccessGroupId())
                    .withDataGroup(dataGroupIds));

            providers.add(new Provider()
                    .withId(externalProviderLegalEntityId)
                    .withAdmins(externalProviderUserIds)
                    .withUsers(externalProviderUserIds));

            consumers.add(new Consumer()
                    .withId(externalConsumerLegalEntityId)
                    .withAdmins(externalConsumerUserIds)
                    .withFunctionDataGroupPairs(functionDataGroupPairs));

            serviceAgreementsIntegrationRestClient.ingestServiceAgreement(serviceAgreementsDataGenerator.generateServiceAgreementPostRequestBody(providers, consumers));
            LOGGER.info(String.format("Service agreement ingested for provider legal entity [%s], provider users %s, consumer legal entity [%s], consumer users %s, master service agreement, function group [%s], data groups %s", externalProviderLegalEntityId, externalProviderUserIds, externalConsumerLegalEntityId, externalConsumerUserIds, functionGroup.getFunctionAccessGroupId(), dataGroupIds));
        }
    }
}
