package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.TRANSACTION_TYPES;
import static com.backbase.ct.bbfuel.data.CommonConstants.TRANSACTION_TYPE_GROUPS;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.github.javafaker.Faker;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.Iban;

public class TransactionsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();
    private static final String EUR_CURRENCY = "EUR";

    public static TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId,
        String category) {
        return new TransactionsPostRequestBody().withId(UUID.randomUUID().toString())
            .withArrangementId(externalArrangementId)
            .withReference(faker.lorem().characters(10))
            .withDescription(faker.lorem().sentence().replace(".", ""))
            .withTypeGroup(TRANSACTION_TYPE_GROUPS.get(random.nextInt(TRANSACTION_TYPE_GROUPS.size())))
            .withType(TRANSACTION_TYPES.get(random.nextInt(TRANSACTION_TYPES.size())))
            .withCategory(category != null ? category : faker.company().industry())
            .withBookingDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-365, 0)))
            .withValueDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-365, 0)))
            .withAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
            .withCurrency(EUR_CURRENCY)
            .withCreditDebitIndicator(TransactionsPostRequestBody.CreditDebitIndicator.values()[random
                .nextInt(TransactionsPostRequestBody.CreditDebitIndicator.values().length)])
            .withInstructedAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
            .withInstructedCurrency(EUR_CURRENCY)
            .withCurrencyExchangeRate(CommonHelpers.generateRandomAmountInRange(1L, 2L))
            .withCounterPartyName(faker.name().fullName())
            .withCounterPartyAccountNumber(Iban.random().toString())
            .withCounterPartyBIC(faker.finance().bic())
            .withCounterPartyCountry(faker.address().countryCode())
            .withCounterPartyBankName(faker.company().name());
    }
}
