package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.POCKETS_CURRENCY;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.pocket.tailor.client.v1.model.Currency;
import com.backbase.dbs.pocket.tailor.client.v1.model.PocketGoalRequest;
import com.backbase.dbs.pocket.tailor.client.v1.model.PocketPostRequest;
import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PocketsDataGenerator {

    private static final Faker faker = new Faker();
    private static final GlobalProperties GLOBAL_PROPERTIES = GlobalProperties.getInstance();

    private PocketsDataGenerator() {
        throw new IllegalStateException("DataGenerator class");
    }

    /**
     * Generate PocketPostRequest.
     * @return PocketPostRequest
     */
    public static PocketPostRequest generatePocketPostRequest() {

        BigDecimal amount = CommonHelpers.generateRandomAmountInRange(100L, 9999L);
        String currency = GLOBAL_PROPERTIES.getString(POCKETS_CURRENCY);

        return new PocketPostRequest()
            .name(faker.lorem().characters(80))
            .icon(faker.lorem().characters(80))
            .goal(new PocketGoalRequest()
                .amountCurrency(new Currency().amount(amount.toString()).currencyCode(currency))
                .deadline(LocalDate.now().plusMonths(CommonHelpers.generateRandomNumberInRange(1, 24))));
    }
}
