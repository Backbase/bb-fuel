package com.backbase.ct.bbfuel.data;

import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ParticipantCreateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementCreateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementUpdateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.Status;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public class ServiceAgreementsDataGenerator {

    private static Faker faker = new Faker();

    public static ServiceAgreementCreateRequest generateServiceAgreementPostRequestBody(
        List<ParticipantCreateRequest> participants) {
        String randomLegalEntityName = faker.name().lastName() + " " +
            faker.company().industry().replaceAll("(/| or).*", "").trim();

        return new ServiceAgreementCreateRequest()
            .name(randomLegalEntityName)
            .description(randomLegalEntityName)
            .externalId(UUID.randomUUID().toString())
            .status(Status.ENABLED)
            .participants(participants);
    }

    public static ServiceAgreementUpdateRequest generateServiceAgreementPutRequestBody() {
        return new ServiceAgreementUpdateRequest()
            .externalId(UUID.randomUUID().toString())
            .status(Status.ENABLED)
            .name(faker.company().name())
            .description(faker.company().catchPhrase());
    }
}
