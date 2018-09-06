package com.backbase.ct.dataloader.data;

import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.github.javafaker.Faker;
import com.google.common.base.Strings;
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

    public static LegalEntitiesPostRequestBody composeLegalEntitiesPostRequestBody(String legalEntityExternalId,
        String legalEntityName,
        String parentLegalEntityExternalId, String type) {
        String randomLegalEntityName = faker.name().lastName() + " " + faker.company().industry();

        return new LegalEntitiesPostRequestBody()
            .withExternalId(Optional.ofNullable(legalEntityExternalId).orElse(generateExternalLegalEntityId()))
            .withName(Optional.ofNullable(legalEntityName).orElse(randomLegalEntityName))
            .withParentExternalId(
                Optional.ofNullable(parentLegalEntityExternalId).orElse(CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID))
            .withType((!Strings.isNullOrEmpty(type)) ? LegalEntityType.fromValue(type) : LegalEntityType.CUSTOMER);
    }

    public static UsersPostRequestBody generateUsersPostRequestBody(String userId, String legalEntityId) {
        return new UsersPostRequestBody()
            .withExternalId(userId)
            .withLegalEntityExternalId(legalEntityId)
            .withFullName(faker.name().firstName() + " " + faker.name().lastName());
    }

    private static String generateExternalLegalEntityId() {
        return CommonConstants.EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }
}
