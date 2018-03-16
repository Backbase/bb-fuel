package com.backbase.testing.dataloader.data;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_LEGAL_ENTITY_ID_PREFIX;

import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.github.javafaker.Faker;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;

public class LegalEntitiesAndUsersDataGenerator {

    private static Faker faker = new Faker();

    public static LegalEntitiesPostRequestBody generateRootLegalEntitiesPostRequestBody(String externalLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(externalLegalEntityId)
            .withName(faker.lorem().sentence(3, 0).replace(".", ""))
            .withParentExternalId(null)
            .withType(LegalEntityType.BANK);
    }

    public static LegalEntitiesPostRequestBody composeLegalEntitiesPostRequestBody(String legalEntityExternalId, String legalEntityName,
        String parentLegalEntityExternalId, String type) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(Optional.ofNullable(legalEntityExternalId).orElse(generateExternalLegalEntityId()))
            .withName(Optional.ofNullable(legalEntityName).orElse(faker.lorem().sentence(3, 0)
                .replace(".", "")))
            .withParentExternalId(parentLegalEntityExternalId)
            .withType(Optional.ofNullable(LegalEntityType.fromValue(type)).orElse(LegalEntityType.CUSTOMER));
    }

    public static UsersPostRequestBody generateUsersPostRequestBody(String userId, String legalEntityId) {
        return new UsersPostRequestBody()
            .withExternalId(userId)
            .withLegalEntityExternalId(legalEntityId)
            .withFullName(faker.name().firstName() + " " + faker.name().lastName());
    }

    private static String generateExternalLegalEntityId() {
        return EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }
}
