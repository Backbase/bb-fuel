package com.backbase.ct.bbfuel.service.factory;

import com.github.javafaker.Faker;

public class TestData {

    private static final Faker FAKER = new Faker();

    public static final String EXTERNAL_SERVICE_AGREEMENT_ID_1 = FAKER.numerify("EXT_SA_######");

    public static final String EXTERNAL_SERVICE_AGREEMENT_ID_2 = FAKER.numerify("EXT_SA_######");

    public static final String EXTERNAL_LEGAL_ENTITY_ID = FAKER.numerify("EXT_LE_######");
}
