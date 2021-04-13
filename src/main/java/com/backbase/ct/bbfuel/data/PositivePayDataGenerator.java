package com.backbase.ct.bbfuel.data;

import com.github.javafaker.Faker;
import com.backbase.dbs.positivepay.client.api.v1.model.PositivePayPost;
import com.backbase.dbs.positivepay.client.api.v1.model.Currency;
import org.apache.commons.lang.time.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;

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
                .issueDate(generateRandomDate())
                .voidCheck(random.nextBoolean());
    }

    public LocalDate generateRandomDate()
    {
        LocalDate from = LocalDate.of(2021, 02, 1);
        LocalDate to = LocalDate.now();
        long days = from.until(to.plusDays(10), ChronoUnit.DAYS);
        long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
        LocalDate randomDate = from.plusDays(randomDays);;
        return randomDate;
    }
}
