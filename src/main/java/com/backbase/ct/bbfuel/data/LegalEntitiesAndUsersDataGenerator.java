package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.EXTERNAL_LEGAL_ENTITY_ID_PREFIX;
import static com.backbase.ct.bbfuel.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;

import com.backbase.ct.bbfuel.dto.User;
import com.backbase.dbs.accesscontrol.ac_legalentity.integration.v3.model.LegalEntityItem;
import com.backbase.dbs.accesscontrol.ac_legalentity.integration.v3.model.LegalEntityType;
import com.backbase.dbs.user.manager.integration.api.v2.model.UserExternal;
import com.github.javafaker.Faker;
import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;

public class LegalEntitiesAndUsersDataGenerator {

    private static Faker faker = new Faker();

    public static LegalEntityItem generateRootLegalEntitiesPostRequestBody(String externalLegalEntityId) {
        return new LegalEntityItem()
            .externalId(externalLegalEntityId)
            .name("Bank")
            .parentExternalId(null)
            .type(LegalEntityType.BANK);
    }

    public static LegalEntityItem composeLegalEntitiesPostRequestBody(String legalEntityExternalId,
        String legalEntityName,
        String parentLegalEntityExternalId, String type) {
        String randomLegalEntityName = faker.name().lastName() + " "
            + faker.company().industry().replaceAll("(/| or).*", "").trim();

        return new LegalEntityItem()
            .externalId(Optional.ofNullable(legalEntityExternalId).orElse(generateExternalLegalEntityId()))
            .name(Optional.ofNullable(legalEntityName).orElse(randomLegalEntityName))
            .parentExternalId(
                Optional.ofNullable(parentLegalEntityExternalId).orElse(EXTERNAL_ROOT_LEGAL_ENTITY_ID))
            .type((!Strings.isNullOrEmpty(type)) ? LegalEntityType.fromValue(type) : LegalEntityType.CUSTOMER);
    }

    public static UserExternal generateUsersPostRequestBody(User user, String legalEntityId) {
        return new UserExternal()
            .withExternalId(user.getExternalId())
            .withLegalEntityExternalId(legalEntityId)
            .withFullName(user.getFullName());
    }

    private static String generateExternalLegalEntityId() {
        return EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }
}
