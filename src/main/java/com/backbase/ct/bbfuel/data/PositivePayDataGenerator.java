package com.backbase.ct.bbfuel.data;

import com.github.javafaker.Faker;
import com.backbase.dbs.positivepay.client.api.v1.model.PositivePayPost;
import com.backbase.dbs.positivepay.client.api.v1.model.Currency;

import java.time.LocalDate;
import java.util.Random;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomDateInRange;

public class PositivePayDataGenerator {

    private  Faker faker = new Faker();
    private  Random random = new Random();

    private Currency amountDetails = new Currency()
            .amount(generateRandomAmountInRange(1000L, 999999999999L).toString())
            .currencyCode("USD");

    public PositivePayPost generatePositivePayPostRequestBody(String internalArrangementId) {

        return new PositivePayPost()
                .arrangementId(internalArrangementId)
                .payeeName(faker.name().firstName() + ' ' + faker.name().lastName())
                .checkNumber(String.valueOf(generateRandomNumberInRange(234567,1234567890)))
                .amountDetails(amountDetails)
                .issueDate(generateRandomDateInRange(LocalDate.now().minusDays(40),LocalDate.now().plusDays(10)))
                .voidCheck(random.nextBoolean());
    }

}
