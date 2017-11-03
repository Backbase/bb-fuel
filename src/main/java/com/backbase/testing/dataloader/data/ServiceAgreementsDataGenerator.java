package com.backbase.testing.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.FunctionDataGroupPair;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.github.javafaker.Faker;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServiceAgreementsDataGenerator {

    private Faker faker = new Faker();

    public ServiceAgreementPostRequestBody generateServiceAgreementPostRequestBody(Set<Provider> providers, Set<Consumer> consumers) {
        return new ServiceAgreementPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalId(UUID.randomUUID().toString())
                .withProviders(providers)
                .withConsumers(consumers);
    }

    public Set<FunctionDataGroupPair> createListOfFunctionGroupDataGroupPairs(Map<String, Set<String>> pairs) {
        return pairs
                .entrySet()
                .stream()
                .map(participants -> setPair(participants.getKey(), participants.getValue()))
                .collect(Collectors.toSet());
    }

    private FunctionDataGroupPair setPair(String fg, Set<String> dgs) {
        return new FunctionDataGroupPair()
                .withFunctionGroup(fg)
                .withDataGroup(dgs);
    }
}
