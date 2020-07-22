package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.*;

import com.backbase.ct.bbfuel.dto.User;
import com.backbase.dbs.user.integration.rest.spec.v2.users.UsersPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.github.javafaker.Faker;
import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;

public class LegalEntitiesAndUsersDataGenerator {

    private static Faker faker = new Faker();

    public static LegalEntitiesPostRequestBody generateRootLegalEntitiesPostRequestBody(String externalLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(externalLegalEntityId)
            .withName("Bank")
            .withParentExternalId(null)
            .withType(LegalEntityType.BANK);
    }

    public static LegalEntitiesPostRequestBody composeLegalEntitiesPostRequestBody(String legalEntityExternalId,
        String legalEntityName,
        String parentLegalEntityExternalId, String type) {
        String randomLegalEntityName = faker.name().lastName() + " "
            + faker.company().industry().replaceAll("(/| or).*", "").trim();

        return new LegalEntitiesPostRequestBody()
            .withExternalId(Optional.ofNullable(legalEntityExternalId).orElse(generateExternalLegalEntityId()))
            .withName(Optional.ofNullable(legalEntityName).orElse(randomLegalEntityName))
            .withParentExternalId(
                Optional.ofNullable(parentLegalEntityExternalId).orElse(EXTERNAL_ROOT_LEGAL_ENTITY_ID))
            .withType((!Strings.isNullOrEmpty(type)) ? LegalEntityType.fromValue(type) : LegalEntityType.CUSTOMER);
    }

    public static UsersPostRequestBody generateUsersPostRequestBody(User user, String legalEntityId) {
        return new UsersPostRequestBody()
            .withExternalId(user.getExternalId())
            .withLegalEntityExternalId(legalEntityId)
            .withFullName(user.getFullName());
    }

    private static String generateExternalLegalEntityId() {
        return EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }
}
