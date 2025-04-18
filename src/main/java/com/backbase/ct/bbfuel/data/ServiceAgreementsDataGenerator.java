package com.backbase.ct.bbfuel.data;

import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.CreateStatus;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.Participant;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.ServiceAgreement;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.ServiceAgreementPut;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public class ServiceAgreementsDataGenerator {

    private static Faker faker = new Faker();

    public static ServiceAgreement generateServiceAgreementPostRequestBody(
        List<Participant> participants) {
        String randomLegalEntityName = faker.name().lastName() + " " +
          faker.company().industry().replaceAll("(/| or).*", "").trim();

        return new ServiceAgreement()
            .name(randomLegalEntityName)
            .description(randomLegalEntityName)
            .externalId(UUID.randomUUID().toString())
            .status(CreateStatus.ENABLED)
            .participants(participants);
    }

    public static ServiceAgreementPut generateServiceAgreementPutRequestBody() {
        return new ServiceAgreementPut()
            .externalId(UUID.randomUUID().toString())
            .name(faker.company().name())
            .description(faker.company().catchPhrase());
    }
}
