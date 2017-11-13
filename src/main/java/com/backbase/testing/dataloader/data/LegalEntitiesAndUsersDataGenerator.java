package com.backbase.testing.dataloader.data;

import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.github.javafaker.Faker;
import org.apache.commons.lang.RandomStringUtils;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_LEGAL_ENTITY_ID_PREFIX;

public class LegalEntitiesAndUsersDataGenerator {

    private Faker faker = new Faker();

    public String generateExternalLegalEntityId() {
        return EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }

    public LegalEntitiesPostRequestBody generateRootLegalEntitiesPostRequestBody(String externalLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
                .withExternalId(externalLegalEntityId)
                .withName(faker.company().name() + faker.lorem().characters(8))
                .withParentExternalId(null)
                .withType(LegalEntityType.BANK);
    }

    public LegalEntitiesPostRequestBody generateLegalEntitiesPostRequestBody(String externalLegalEntityId, String externalParentLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
                .withExternalId(externalLegalEntityId)
                .withName(faker.company().name() + faker.lorem().characters(8))
                .withParentExternalId(externalParentLegalEntityId)
                .withType(LegalEntityType.CUSTOMER);
    }

    public UsersPostRequestBody generateUsersPostRequestBody(String externalUserId, String externalLegalEntityId) {
        return new UsersPostRequestBody()
                .withExternalId(externalUserId)
                .withLegalEntityExternalId(externalLegalEntityId)
                .withFullName(faker.name().fullName());
    }
}
