package com.backbase.ct.bbfuel.data;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody.CreditDebitIndicator;
import com.github.javafaker.Faker;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.Iban;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.backbase.ct.bbfuel.data.CommonConstants.IBAN_ACCOUNT_TYPE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_ACCOUNT_TYPES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_TRANSACTIONS_BUSINESS_CURRENCY;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody.CreditDebitIndicator.CRDT;
import static java.util.Arrays.asList;

public class TransactionsDataGenerator {

    private static Faker faker = new Faker();
    private static final GlobalProperties GLOBAL_PROPERTIES = GlobalProperties.getInstance();
    private static final List<String> TRANSACTION_TYPE_GROUPS = asList(
        "Payment",
        "Withdrawal",
        "Loans",
        "Fees"
    );

    private static final List<String> TRANSACTION_TYPES = asList(
        "ATM",
        "ACH",
        "Bill Payment",
        "Cash",
        "Cheques",
        "Credit/Debit Card",
        "Check",
        "Deposit",
        "Fee",
        "POS",
        "Withdrawal"
    );
    private static final List<String> DEBIT_BUSINESS_CATEGORIES = asList(
        "Suppliers",
        "Salaries",
        "Office rent",
        "Loan repayment",
        "Miscellaneous"
    );
    private static final List<String> CREDIT_BUSINESS_CATEGORIES = asList(
        "Intercompany receivable",
        "Intracompany receivable",
        "Direct debit collections",
        "Interest received",
        "Term deposit"
    );

    private static List<String> US_COUNTER_PARTY_NAMES = asList(
        "Shell",
        "Netflix",
        "Exxon",
        "Walmart",
        "Verizon",
        "T-Mobile",
        "Amazon",
        "E-bay",
        "The Home Depot",
        "McDonald's",
        "Spotify",
        "Starbucks",
        "Subway",
        "Lowe's",
        "Walgreens",
        "Dollar Tree",
        "Best Buy",
        "United States Postal Service"
    );

    private static List<String> TRANSACTION_DESCRIPTIONS = asList(
        "5342423424-RECURRING",
        "D8CD8C8XFS888-PAY",
        "PORTLAND2332-PAY",
        "S-234243554574542312",
        "AAA1122353",
        "RECURRING-23442424243"
    );

    public static TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId) {
        CreditDebitIndicator creditDebitIndicator = getRandomFromEnumValues(CreditDebitIndicator.values());

        String finalCategory = creditDebitIndicator == CRDT
            ? getRandomFromList(CREDIT_BUSINESS_CATEGORIES)
            : getRandomFromList(DEBIT_BUSINESS_CATEGORIES);

        String description = faker.lorem().sentence().replace(".", "");
        String counterPartyName = faker.name().fullName();

        BigDecimal amount = CommonHelpers.generateRandomAmountInRange(100L, 9999L);
        String currency = GLOBAL_PROPERTIES.getString(PROPERTY_TRANSACTIONS_BUSINESS_CURRENCY);

        String accountNumber = GLOBAL_PROPERTIES.getString(PROPERTY_CONTACTS_ACCOUNT_TYPES).contains(IBAN_ACCOUNT_TYPE) ?
            Iban.random().toString() :
            String.valueOf(generateRandomNumberInRange(100000, 999999999));


        return new TransactionsPostRequestBody().withId(UUID.randomUUID().toString())
            .withArrangementId(externalArrangementId)
            .withReference(faker.lorem().characters(10))
            .withDescription(description)
            .withTypeGroup(getRandomFromList(TRANSACTION_TYPE_GROUPS))
            .withType(getRandomFromList(TRANSACTION_TYPES))
            .withCategory(finalCategory)
            .withBookingDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-365, 0)))
            .withValueDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-365, 0)))
            .withAmount(amount)
            .withCurrency(currency)
            .withCreditDebitIndicator(creditDebitIndicator)
            .withInstructedAmount(amount)
            .withInstructedCurrency(currency)
            .withCurrencyExchangeRate(CommonHelpers.generateRandomAmountInRange(1L, 2L))
            .withCounterPartyName(counterPartyName)
            .withCounterPartyAccountNumber(accountNumber)
            .withCounterPartyBIC(faker.finance().bic())
            .withCounterPartyCountry(faker.address().countryCode())
            .withCounterPartyBankName(faker.company().name());
    }
}
