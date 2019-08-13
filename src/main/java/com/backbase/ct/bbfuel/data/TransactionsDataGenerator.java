package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody.CreditDebitIndicator.CRDT;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody.CreditDebitIndicator;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.SubCategory;
import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.Iban;

public class TransactionsDataGenerator {

    private static Faker faker = new Faker();
    private static final String EUR_CURRENCY = "EUR";
    private static final String USD_CURRENCY = "USD";
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
    private static List<String> DEBIT_RETAIL_CATEGORIES = asList(
        "Food Drinks",
        "Transportation",
        "Home",
        "Health Beauty",
        "Shopping",
        "Bills Utilities",
        "Hobbies Entertainment",
        "Transfers",
        "Uncategorised",
        "Car",
        "Beauty",
        "Health Fitness",
        "Mortgage",
        "Rent",
        "Public Transport",
        "Internet",
        "Mobile Phone",
        "Utilities",
        "Alcohol Bars",
        "Fast Food",
        "Groceries",
        "Restaurants",
        "Clothing",
        "Electronics"
    );
    private static List<String> CREDIT_RETAIL_CATEGORIES = asList(
        "Income",
        "Other Income",
        "Bonus",
        "Salary/Wages",
        "Interest Income",
        "Rental Income"
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

    public static TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId,
        boolean isRetail, List<SubCategory> categories) {
        CreditDebitIndicator creditDebitIndicator = getRandomFromEnumValues(CreditDebitIndicator.values());

        BigDecimal amount;

        String currency;
        String counterPartyName;
        String description;
        String finalCategory;

        if (isRetail) {
            if (!categories.isEmpty()) {
                CREDIT_RETAIL_CATEGORIES = categories.stream()
                    .filter(category -> "INCOME".equals(category.getCategoryType()))
                    .map(SubCategory::getCategoryName)
                    .collect(Collectors.toList());

                DEBIT_RETAIL_CATEGORIES = categories.stream()
                    .filter(category -> "EXPENSE".equals(category.getCategoryType()))
                    .map(SubCategory::getCategoryName)
                    .collect(Collectors.toList());
            }

            finalCategory = creditDebitIndicator == CRDT
                ? getRandomFromList(CREDIT_RETAIL_CATEGORIES)
                : getRandomFromList(DEBIT_RETAIL_CATEGORIES);

            amount = creditDebitIndicator == CRDT
                    ? CommonHelpers.generateRandomAmountInRange(1000L, 3000L)
                    : CommonHelpers.generateRandomAmountInRange(1L, 300L);

            description = getRandomFromList(TRANSACTION_DESCRIPTIONS);
            counterPartyName = getRandomFromList(US_COUNTER_PARTY_NAMES);
            currency = USD_CURRENCY;
        } else {
            finalCategory = creditDebitIndicator == CRDT
                ? getRandomFromList(CREDIT_BUSINESS_CATEGORIES)
                : getRandomFromList(DEBIT_BUSINESS_CATEGORIES);

            description = faker.lorem().sentence().replace(".", "");
            counterPartyName = faker.name().fullName();
            amount = CommonHelpers.generateRandomAmountInRange(100L, 9999L);
            currency = EUR_CURRENCY;
        }

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
            .withCounterPartyAccountNumber(Iban.random().toString())
            .withCounterPartyBIC(faker.finance().bic())
            .withCounterPartyCountry(faker.address().countryCode())
            .withCounterPartyBankName(faker.company().name());
    }
}
