package com.backbase.testing.dataloader.data;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_LEGAL_ENTITY_ID_PREFIX;

import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.github.javafaker.Faker;
import org.apache.commons.lang.RandomStringUtils;

public class LegalEntitiesAndUsersDataGenerator {

    private static Faker faker = new Faker();

    public static String generateExternalLegalEntityId() {
        return EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }

    public static LegalEntitiesPostRequestBody generateRootLegalEntitiesPostRequestBody(String externalLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(externalLegalEntityId)
            .withName(faker.lorem().sentence(3, 0).replace(".", ""))
            .withParentExternalId(null)
            .withType(LegalEntityType.BANK);
    }

    @Deprecated
    public static LegalEntitiesPostRequestBody generateLegalEntitiesPostRequestBody(String externalLegalEntityId, String externalParentLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(externalLegalEntityId)
            .withName(faker.lorem().sentence(3, 0).replace(".", ""))
            .withParentExternalId(externalParentLegalEntityId)
            .withType(LegalEntityType.CUSTOMER);
    }

    public static UsersPostRequestBody generateUsersPostRequestBody(String userId, String legalEntityId) {
        return new UsersPostRequestBody()
            .withExternalId(userId)
            .withLegalEntityExternalId(legalEntityId)
            .withFullName(faker.name().firstName() + " " + faker.name().lastName());
    }

    public static LegalEntitiesPostRequestBody composeLegalEntitiesPostRequestBody(String legalEntityExternalId, String legalEntityName,
        String parentLegalEntityExternalId, String type) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(legalEntityExternalId)
            .withName(legalEntityName)
            .withParentExternalId(parentLegalEntityExternalId)
            .withType(LegalEntityType.fromValue(type));
    }
}
