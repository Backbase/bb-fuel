package com.backbase.ct.dataloader.data;

import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.github.javafaker.Faker;
import org.iban4j.Iban;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static com.backbase.ct.dataloader.data.CommonConstants.TRANSACTION_TYPES;
import static com.backbase.ct.dataloader.data.CommonConstants.TRANSACTION_TYPE_GROUPS;

public class TransactionsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();
    private static final String EUR_CURRENCY = "EUR";

    public static TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId) {
        return new TransactionsPostRequestBody().withId(UUID.randomUUID().toString())
                .withArrangementId(externalArrangementId)
                .withReference(faker.lorem().characters(10))
                .withDescription(faker.lorem().sentence().replace(".", ""))
                .withTypeGroup(TRANSACTION_TYPE_GROUPS.get(random.nextInt(TRANSACTION_TYPE_GROUPS.size())))
                .withType(TRANSACTION_TYPES.get(random.nextInt(TRANSACTION_TYPES.size())))
                .withCategory(faker.company().industry())
                .withBookingDate(new Date())
                .withValueDate(new Date())
                .withAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
                .withCurrency(EUR_CURRENCY)
                .withCreditDebitIndicator(TransactionsPostRequestBody.CreditDebitIndicator.values()[random.nextInt(TransactionsPostRequestBody.CreditDebitIndicator.values().length)])
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
