package com.backbase.testing.dataloader.data;

import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.github.javafaker.Faker;
import org.iban4j.Iban;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class TransactionsDataGenerator {

    private Faker faker = new Faker();
    private Random random = new Random();

    public TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId) {

        return new TransactionsPostRequestBody().withId(UUID.randomUUID().toString())
                .withArrangementId(externalArrangementId)
                .withReference(faker.lorem().characters(10))
                .withDescription(faker.lorem().sentence().replace(".", ""))
                .withTypeGroup(TransactionsPostRequestBody.TypeGroup.values()[random.nextInt(TransactionsPostRequestBody.TypeGroup.values().length)])
                .withType(TransactionsPostRequestBody.Type.values()[random.nextInt(TransactionsPostRequestBody.Type.values().length)])
                .withCategory(faker.company().industry())
                .withBookingDate(new Date())
                .withValueDate(new Date())
                .withAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
                .withCurrency(TransactionsPostRequestBody.Currency.EUR)
                .withCreditDebitIndicator(TransactionsPostRequestBody.CreditDebitIndicator.values()[random.nextInt(TransactionsPostRequestBody.CreditDebitIndicator.values().length)])
                .withInstructedAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
                .withInstructedCurrency(TransactionsPostRequestBody.Currency.EUR)
                .withCurrencyExchangeRate(CommonHelpers.generateRandomAmountInRange(1L, 2L))
                .withCounterPartyName(faker.name().toString())
                .withCounterPartyAccountNumber(Iban.random().toString())
                .withCounterPartyBIC(faker.finance().bic())
                .withCounterPartyCountry(faker.address().countryCode())
                .withCounterPartyBankName(faker.company().name());
    }
}
