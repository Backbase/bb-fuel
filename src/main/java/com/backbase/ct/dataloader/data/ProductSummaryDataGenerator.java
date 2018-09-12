package com.backbase.ct.dataloader.data;

import static com.backbase.ct.dataloader.data.ArrangementType.FINANCE_INTERNATIONAL;
import static com.backbase.ct.dataloader.data.ArrangementType.GENERAL_BUSINESS;
import static com.backbase.ct.dataloader.data.ArrangementType.GENERAL_RETAIL;
import static com.backbase.ct.dataloader.data.ArrangementType.INTERNATIONAL_TRADE;
import static com.backbase.ct.dataloader.data.ArrangementType.PAYROLL;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_PAYROLL_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_PAYROLL_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_DEBIT_CARDS_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_DEBIT_CARDS_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_PRODUCTS_JSON_LOCATION;
import static com.backbase.ct.dataloader.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.dataloader.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.AccountHolderCountry;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.ct.dataloader.util.ParserUtil;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.DebitCard;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class ProductSummaryDataGenerator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static Faker faker = new Faker();
    private static Random random = new Random();
    private static final List<CountryCode> COUNTRY_CODES;
    private static final int WEEKS_IN_A_QUARTER = 13;
    private static final List<String> GENERAL_BUSINESS_CURRENT_ACCOUNT_NAMES = asList(
        "Factory",
        "GBF Corporate",
        "Legal",
        "Manufacturing",
        "Marketing",
        "Payables",
        "Personnel",
        "Receivables",
        "Sales",
        "Support"
    );

    private static final List<String> INTERNATIONAL_TRADE_CURRENT_ACCOUNT_NAMES = asList(
        "Bahrain sales",
        "China sales",
        "Japan sales",
        "France sales",
        "India sales",
        "Turkey sales",
        "Belgium sales"
    );

    private static final List<String> FINANCE_INTERNATIONAL_CURRENT_ACCOUNT_NAMES = asList(
        "Assets",
        "Liability",
        "Equity",
        "Revenue",
        "Expenses",
        "Executive expenses",
        "Transport"
    );

    private static final Map<ArrangementType, List<String>> CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP = new HashMap<>();
    static {
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(GENERAL_RETAIL, singletonList("Current Account"));
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(GENERAL_BUSINESS, GENERAL_BUSINESS_CURRENT_ACCOUNT_NAMES);
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(INTERNATIONAL_TRADE, INTERNATIONAL_TRADE_CURRENT_ACCOUNT_NAMES);
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP
            .put(FINANCE_INTERNATIONAL, FINANCE_INTERNATIONAL_CURRENT_ACCOUNT_NAMES);
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(PAYROLL, singletonList("Payroll"));
    }

    private static final Map<Integer, List<String>> PRODUCT_ARRANGEMENT_NAME_MAP = new HashMap<>();
    static {
        PRODUCT_ARRANGEMENT_NAME_MAP.put(2, singletonList("Savings"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(3, singletonList("Credit Card"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(4, singletonList("Loan"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(5, singletonList("Term Deposit"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(6, singletonList("Investments"));
    }

    static {
        List<String> allowed = asList("AT", "BE", "BG", "CH", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB",
            "GI", "GR", "HR",
            "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK",
            "SM");
        COUNTRY_CODES = new ArrayList<>();
        CountryCode[] values = CountryCode.values();
        for (CountryCode code : values) {
            if (allowed.contains(code.name())) {
                COUNTRY_CODES.add(code);
            }
        }
    }


    private static final Map<ArrangementType, BiFunction<String, Currency, List<ArrangementsPostRequestBody>>> FUNCTION_MAP = new HashMap<>();
    static {
        FUNCTION_MAP.put(GENERAL_RETAIL, ProductSummaryDataGenerator::generateGeneralRetailArrangements);
        FUNCTION_MAP.put(GENERAL_BUSINESS, ProductSummaryDataGenerator::generateGeneralBusinessArrangements);
        FUNCTION_MAP.put(INTERNATIONAL_TRADE, ProductSummaryDataGenerator::generateInternationalTradeArrangements);
        FUNCTION_MAP.put(FINANCE_INTERNATIONAL, ProductSummaryDataGenerator::generateFinanceInternationalArrangements);
        FUNCTION_MAP.put(PAYROLL, ProductSummaryDataGenerator::generatePayrollArrangements);
    }

    private static Map<ArrangementType, Integer> ARRANGEMENT_TYPE_AMOUNT_MAP = new HashMap<>();
    static {
        ARRANGEMENT_TYPE_AMOUNT_MAP.put(GENERAL_RETAIL, generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MAX)));
        ARRANGEMENT_TYPE_AMOUNT_MAP.put(GENERAL_BUSINESS, generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MAX)));
        ARRANGEMENT_TYPE_AMOUNT_MAP.put(INTERNATIONAL_TRADE, generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MAX)));
        ARRANGEMENT_TYPE_AMOUNT_MAP.put(FINANCE_INTERNATIONAL, generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MAX)));
        ARRANGEMENT_TYPE_AMOUNT_MAP.put(PAYROLL, generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_PAYROLL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_PAYROLL_MAX)));
    }

    public static String generateRandomIban() {
        return Iban.random(COUNTRY_CODES.get(random.nextInt(COUNTRY_CODES.size()))).toString();
    }

    public static ProductsPostRequestBody[] generateProductsPostRequestBodies() throws IOException {
        return ParserUtil
            .convertJsonToObject(globalProperties.getString(PROPERTY_PRODUCTS_JSON_LOCATION),
                ProductsPostRequestBody[].class);
    }

    public static List<ArrangementsPostRequestBody> generateArrangementsPostRequestBodies(String externalLegalEntityId,
        Currency currency, ArrangementType arrangementType) {

        return FUNCTION_MAP.get(arrangementType).apply(externalLegalEntityId, currency);
    }

    private static List<ArrangementsPostRequestBody> generateFinanceInternationalArrangements(String externalLegalEntityId, Currency currency) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<String> financeInternationalCurrencyList = asList("EUR", "USD", "CAD", "GBP");
        Currency financeInternationalCurrency = currency == null
            ? Currency.valueOf(financeInternationalCurrencyList.get(random.nextInt(financeInternationalCurrencyList.size())))
            : currency;
        int numberOfLoanAndSavingsAccountsPerGroup = 2;
        int numberOfInvestmentsAccountsPerGroup = 3;
        int numberOfCurrentAccountArrangements = ARRANGEMENT_TYPE_AMOUNT_MAP.get(FINANCE_INTERNATIONAL) <=
            (numberOfLoanAndSavingsAccountsPerGroup + numberOfInvestmentsAccountsPerGroup)
            ? ARRANGEMENT_TYPE_AMOUNT_MAP.get(FINANCE_INTERNATIONAL)
            : ARRANGEMENT_TYPE_AMOUNT_MAP.get(FINANCE_INTERNATIONAL) - (numberOfLoanAndSavingsAccountsPerGroup + numberOfInvestmentsAccountsPerGroup);

        IntStream.range(0, numberOfCurrentAccountArrangements).parallel().forEach(randomNumber ->
            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, financeInternationalCurrency, FINANCE_INTERNATIONAL)));

        if (ARRANGEMENT_TYPE_AMOUNT_MAP.get(FINANCE_INTERNATIONAL) > (numberOfLoanAndSavingsAccountsPerGroup + numberOfInvestmentsAccountsPerGroup)) {
            IntStream.range(0, numberOfInvestmentsAccountsPerGroup).parallel().forEach(randomNumber -> arrangementsPostRequestBodies.add(
                generateArrangementsPostRequestBody(externalLegalEntityId,
                    6, financeInternationalCurrency,
                    FINANCE_INTERNATIONAL)));

            IntStream.range(0, numberOfLoanAndSavingsAccountsPerGroup).parallel().forEach(randomNumber -> {
                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId,
                        2, financeInternationalCurrency,
                        FINANCE_INTERNATIONAL));

                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId,
                        4, financeInternationalCurrency,
                        FINANCE_INTERNATIONAL));
            });
        }
        return arrangementsPostRequestBodies;
    }

    private static List<ArrangementsPostRequestBody> generatePayrollArrangements(String externalLegalEntityId, Currency currency) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<String> payrollCurrencyList = asList("EUR", "GBP", "USD", "CAD");
        Currency payrollCurrency = currency == null
            ? Currency.valueOf(payrollCurrencyList.get(random.nextInt(payrollCurrencyList.size())))
            : currency;
        IntStream.range(0, ARRANGEMENT_TYPE_AMOUNT_MAP.get(PAYROLL)).parallel().forEach(randomNumber ->
            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, payrollCurrency, PAYROLL)));

        return arrangementsPostRequestBodies;
    }

    private static List<ArrangementsPostRequestBody> generateInternationalTradeArrangements(String externalLegalEntityId, Currency currency) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<String> internationalTradeCurrencyList = asList("BHD", "CNY", "JPY", "EUR", "INR", "TRY", "EUR");
        Currency internationalTradeCurrency = currency == null
            ? Currency.valueOf(internationalTradeCurrencyList.get(random.nextInt(internationalTradeCurrencyList.size())))
            : currency;
        IntStream.range(0, ARRANGEMENT_TYPE_AMOUNT_MAP.get(INTERNATIONAL_TRADE)).parallel().forEach(randomNumber ->
            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, internationalTradeCurrency, INTERNATIONAL_TRADE)));

        return arrangementsPostRequestBodies;
    }

    private static List<ArrangementsPostRequestBody> generateGeneralBusinessArrangements(String externalLegalEntityId, Currency currency) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<String> arrangementCurrencyList = asList("EUR", "USD", "CAD", "GBP");
        Currency arrangementCurrency = currency == null
            ? Currency.valueOf(arrangementCurrencyList.get(random.nextInt(arrangementCurrencyList.size())))
            : currency;

        int numberOfSavingsAccountsPerGroup = 1;
        int numberOfCurrentAccountArrangements = ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_BUSINESS) <= numberOfSavingsAccountsPerGroup
            ? ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_BUSINESS)
            : ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_BUSINESS) - numberOfSavingsAccountsPerGroup;

        IntStream.range(0, numberOfCurrentAccountArrangements).parallel().forEach(randomNumber ->
            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, arrangementCurrency,
                    GENERAL_BUSINESS)));

        if (ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_BUSINESS) > numberOfSavingsAccountsPerGroup) {
            IntStream.range(0, numberOfSavingsAccountsPerGroup).parallel().forEach(randomNumber ->
                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId, 2, arrangementCurrency,
                        GENERAL_BUSINESS)));
        }

        return arrangementsPostRequestBodies;
    }

    private static List<ArrangementsPostRequestBody> generateGeneralRetailArrangements(String externalLegalEntityId, Currency currency) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<String> arrangementCurrencyList = asList("EUR", "USD", "CAD", "GBP");
        Currency arrangementCurrency = currency == null
            ? Currency.valueOf(arrangementCurrencyList.get(random.nextInt(arrangementCurrencyList.size())))
            : currency;

        final int NUMBER_OF_PRODUCTS = 6;

        int numberOfCurrentAccountArrangements;

        numberOfCurrentAccountArrangements = ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_RETAIL) <= NUMBER_OF_PRODUCTS
            ? ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_RETAIL)
            : ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_RETAIL) - (NUMBER_OF_PRODUCTS - 1);

        IntStream.range(0, numberOfCurrentAccountArrangements).parallel().forEach(randomNumber ->
            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, arrangementCurrency,
                    GENERAL_BUSINESS)));

        if (ARRANGEMENT_TYPE_AMOUNT_MAP.get(GENERAL_RETAIL) > NUMBER_OF_PRODUCTS) {
            // Any other product than current account
            IntStream.range(2, (NUMBER_OF_PRODUCTS + 1)).parallel().forEach(productId ->
                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId, productId, arrangementCurrency,
                        GENERAL_BUSINESS)));
        }

        return arrangementsPostRequestBodies;
    }

    private static ArrangementsPostRequestBody generateArrangementsPostRequestBody(String externalLegalEntityId,
        int productId, Currency currency, ArrangementType arrangementType) {
        AccountHolderCountry[] accountHolderCountries = AccountHolderCountry.values();
        boolean debitCreditAccountIndicator = productId == 1 || productId == 2;
        final HashSet<DebitCard> debitCards = new HashSet<>();

        if (productId == 1) {
            for (int i = 0;
                i < generateRandomNumberInRange(globalProperties.getInt(PROPERTY_DEBIT_CARDS_MIN),
                    globalProperties.getInt(PROPERTY_DEBIT_CARDS_MAX)); i++) {
                debitCards.add(new DebitCard()
                    .withNumber(String.format("%s", generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }
        }

        String accountNumber = currency.equals(Currency.EUR)
            ? generateRandomIban() : valueOf(generateRandomNumberInRange(0, 999999999));

        String bic = faker.finance().bic();

        String arrangementNameSuffix =
            " " + currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);

        String arrangementName = productId == 1 ? CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(arrangementType)
            .get(random.nextInt(CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(arrangementType).size()))
            + arrangementNameSuffix : PRODUCT_ARRANGEMENT_NAME_MAP.get(productId)
            .get(random.nextInt(PRODUCT_ARRANGEMENT_NAME_MAP.get(productId).size())) + arrangementNameSuffix;

        ArrangementsPostRequestBody arrangementsPostRequestBody = new ArrangementsPostRequestBody()
            .withId(UUID.randomUUID().toString())
            .withLegalEntityId(externalLegalEntityId)
            .withProductId(String.format("%s", productId))
            .withName(arrangementName)
            .withAlias(faker.lorem().characters(10))
            .withBookedBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withAvailableBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withCreditLimit(generateRandomAmountInRange(10000L, 999999L))
            .withCurrency(currency)
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(true)
            .withAccruedInterest(BigDecimal.valueOf(random.nextInt(10)))
            .withNumber(String.format("%s", random.nextInt(9999)))
            .withPrincipalAmount(generateRandomAmountInRange(10000L, 999999L))
            .withCurrentInvestmentValue(generateRandomAmountInRange(10000L, 999999L))
            .withDebitAccount(debitCreditAccountIndicator)
            .withCreditAccount(debitCreditAccountIndicator)
            .withDebitCards(debitCards)
            .withAccountHolderName(faker.name().fullName())
            .withAccountHolderAddressLine1(faker.address().streetAddress())
            .withAccountHolderAddressLine2(faker.address().secondaryAddress())
            .withAccountHolderStreetName(faker.address().streetAddress())
            .withPostCode(faker.address().zipCode())
            .withTown(faker.address().city())
            .withAccountHolderCountry(
                accountHolderCountries[generateRandomNumberInRange(0, accountHolderCountries.length - 1)])
            .withCountrySubDivision(faker.address().state())
            .withBIC(bic);

        if (currency.equals(Currency.EUR)) {
            arrangementsPostRequestBody
                .withIBAN(accountNumber)
                .withBBAN(accountNumber.substring(3).replaceAll("[a-zA-Z]", ""));
        } else {
            arrangementsPostRequestBody
                .withBBAN(accountNumber);
        }

        return arrangementsPostRequestBody;
    }

    public static List<BalanceHistoryPostRequestBody> generateBalanceHistoryPostRequestBodies(
        String externalArrangementId) {
        List<BalanceHistoryPostRequestBody> balanceHistoryPostRequestBodies = new ArrayList<>();

        for (int i = 0; i >= -WEEKS_IN_A_QUARTER; i--) {
            balanceHistoryPostRequestBodies.add(generateBalanceHistoryPostRequestBody(
                externalArrangementId, DateUtils.addWeeks(new Date(), i)));
        }

        return balanceHistoryPostRequestBodies;
    }

    private static BalanceHistoryPostRequestBody generateBalanceHistoryPostRequestBody(String
        externalArrangementId,
        Date updatedDate) {
        return new BalanceHistoryPostRequestBody()
            .withArrangementId(externalArrangementId)
            .withBalance(generateRandomAmountInRange(1000000L, 1999999L))
            .withUpdatedDate(updatedDate);
    }
}
