package com.backbase.ct.bbfuel.data;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.transaction.client.v2.model.TransactionsPostRequestBody;
import com.backbase.dbs.transaction.client.v2.model.TransactionsPostRequestBody.CreditDebitIndicatorEnum;
import com.backbase.dbs.transaction.client.v2.model.Currency;
import com.github.javafaker.Faker;
import org.iban4j.Iban;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.backbase.ct.bbfuel.data.CommonConstants.IBAN_ACCOUNT_TYPE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_ACCOUNT_TYPES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_TRANSACTIONS_CURRENCY;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
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

    public static TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId) {
        CreditDebitIndicatorEnum creditDebitIndicator = getRandomFromEnumValues(CreditDebitIndicatorEnum.values());

        String finalCategory = creditDebitIndicator == CreditDebitIndicatorEnum.CRDT
            ? getRandomFromList(CREDIT_BUSINESS_CATEGORIES)
            : getRandomFromList(DEBIT_BUSINESS_CATEGORIES);

        String description = faker.lorem().sentence().replace(".", "");
        String counterPartyName = faker.name().fullName();

        BigDecimal amount = CommonHelpers.generateRandomAmountInRange(100L, 9999L);
        String currency = getRandomFromList(Arrays.asList(GLOBAL_PROPERTIES.getString(PROPERTY_TRANSACTIONS_CURRENCY).split(",")));

        String accountNumber = GLOBAL_PROPERTIES.getString(PROPERTY_CONTACTS_ACCOUNT_TYPES).contains(IBAN_ACCOUNT_TYPE) ?
            Iban.random().toString() :
            String.valueOf(generateRandomNumberInRange(100000, 999999999));

        return new TransactionsPostRequestBody()
                .id(UUID.randomUUID().toString())
                .arrangementId(externalArrangementId)
                .reference(faker.lorem().characters(10))
                .description(description)
                .typeGroup(getRandomFromList(TRANSACTION_TYPE_GROUPS))
                .type(getRandomFromList(TRANSACTION_TYPES))
                .category(finalCategory)
                .bookingDate(LocalDate.now())
                .valueDate(LocalDate.now())
                .transactionAmountCurrency(new Currency().amount(amount).currencyCode(currency))
                .instructedAmountCurrency(new Currency().amount(amount).currencyCode(currency))
                .currencyExchangeRate(CommonHelpers.generateRandomAmountInRange(1L, 2L))
                .creditDebitIndicator(creditDebitIndicator)
                .counterPartyName(counterPartyName)
                .counterPartyAccountNumber(accountNumber)
                .counterPartyBIC(faker.finance().bic())
                .counterPartyCountry(faker.address().countryCode())
                .counterPartyBankName(faker.company().name());
    }
}
