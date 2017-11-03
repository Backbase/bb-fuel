package com.backbase.testing.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.github.javafaker.Faker;

import java.util.Set;
import java.util.UUID;

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
}
