package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.ArrangementType.FINANCE_INTERNATIONAL;
import static com.backbase.ct.bbfuel.data.ArrangementType.GENERAL_BUSINESS;
import static com.backbase.ct.bbfuel.data.ArrangementType.GENERAL_RETAIL;
import static com.backbase.ct.bbfuel.data.ArrangementType.INTERNATIONAL_TRADE;
import static com.backbase.ct.bbfuel.data.ArrangementType.PAYROLL;
import static com.backbase.ct.bbfuel.data.CommonConstants.CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP;
import static com.backbase.ct.bbfuel.data.CommonConstants.INTERNATIONAL_TRADE_CURRENCY_NAME_MAP;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_DEBIT_CARDS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_DEBIT_CARDS_MIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_PRODUCTS_JSON_LOCATION;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.AccountHolderCountry;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.EUR;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.ct.bbfuel.util.ParserUtil;
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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSummaryDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSummaryDataGenerator.class);


    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static Faker faker = new Faker();
    private static final List<CountryCode> COUNTRY_CODES;
    private static final int WEEKS_IN_A_QUARTER = 13;

    private static final Map<Integer, String> PRODUCT_ARRANGEMENT_NAME_MAP = new HashMap<>();

    static {
        PRODUCT_ARRANGEMENT_NAME_MAP.put(2, "Savings");
        PRODUCT_ARRANGEMENT_NAME_MAP.put(3, "Credit Card");
        PRODUCT_ARRANGEMENT_NAME_MAP.put(4, "Loan");
        PRODUCT_ARRANGEMENT_NAME_MAP.put(5, "Term Deposit");
        PRODUCT_ARRANGEMENT_NAME_MAP.put(6, "Investments");
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
        FUNCTION_MAP.put(GENERAL_RETAIL, ArrangementsDataGenerator::generateGeneralRetailArrangements);
        FUNCTION_MAP.put(GENERAL_BUSINESS, ArrangementsDataGenerator::generateGeneralBusinessArrangements);
        FUNCTION_MAP.put(INTERNATIONAL_TRADE, ArrangementsDataGenerator::generateInternationalTradeArrangements);
        FUNCTION_MAP.put(FINANCE_INTERNATIONAL, ArrangementsDataGenerator::generateFinanceInternationalArrangements);
        FUNCTION_MAP.put(PAYROLL, ArrangementsDataGenerator::generatePayrollArrangements);
    }

    public static String generateRandomIban() {
        return Iban.random(getRandomFromList(COUNTRY_CODES)).toString();
    }

    public static List<ProductsPostRequestBody> generateProductsPostRequestBodies() throws IOException {
        return asList(ParserUtil
            .convertJsonToObject(globalProperties.getString(PROPERTY_PRODUCTS_JSON_LOCATION),
                ProductsPostRequestBody[].class));
    }

    public static String getProductTypeNameFromProductsPostRequestBodies(String productId) throws IOException {
        List<ProductsPostRequestBody> productsPostRequestBodies = asList(ParserUtil
            .convertJsonToObject(globalProperties.getString(PROPERTY_PRODUCTS_JSON_LOCATION),
                ProductsPostRequestBody[].class));

        ProductsPostRequestBody product = productsPostRequestBodies.stream()
            .filter(productsPostRequestBody -> productId.equals(productsPostRequestBody.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No product found by id: %s", productId)));

        return product.getProductTypeName();
    }

    public static synchronized List<ArrangementsPostRequestBody> generateArrangementsPostRequestBodies(
        String externalLegalEntityId,
        ArrangementType arrangementType, Currency currency) {

        return FUNCTION_MAP.get(arrangementType).apply(externalLegalEntityId, currency);
    }

    static ArrangementsPostRequestBody generateArrangementsPostRequestBody(String externalLegalEntityId,
        int productId, Currency currency, ArrangementType arrangementType) {
        return generateArrangementsPostRequestBody(externalLegalEntityId, productId, currency, arrangementType, null);
    }

    private static String constructArrangementName(Integer productId, String currentAccountArrangementName,
        String arrangementNameSuffix) {
        return productId == 1 ? currentAccountArrangementName + arrangementNameSuffix
            : PRODUCT_ARRANGEMENT_NAME_MAP.get(productId) + arrangementNameSuffix;
    }

    public static List<ArrangementsPostRequestBody> generateCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId,
        List<Currency> currencies, List<String> currentAccountNames, int numberOfArrangements) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();

        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            int randomCurrentAccountIndex = ThreadLocalRandom.current().nextInt(currentAccountNames.size());
            // To support specific currency - account name map such as in the International Trade product group example
            int randomCurrencyIndex = currencies.size() == currentAccountNames.size()
                ? randomCurrentAccountIndex : ThreadLocalRandom.current().nextInt(currencies.size());

            int currentAccountNameIndex = randomNumber < currentAccountNames.size() ? randomNumber
                : randomCurrentAccountIndex;
            int currencyIndex = randomNumber < currencies.size() ? randomNumber
                : randomCurrencyIndex;

            String currentAccountName = currentAccountNames.get(currentAccountNameIndex);
            Currency currency = currencies.get(currencyIndex);
            ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
                externalLegalEntityId, currentAccountName, currency, 1);

            arrangementsPostRequestBodies.add(arrangementsPostRequestBody);
        });

        return arrangementsPostRequestBodies;
    }

    public static List<ArrangementsPostRequestBody> generateNonCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId,
        List<Currency> currencies, List<String> productIds, int numberOfArrangements) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();

        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            Currency currency = getRandomFromList(currencies);
            String productId = getRandomFromList(productIds);
            String arrangementName = null;
            try {
                arrangementName = getProductTypeNameFromProductsPostRequestBodies(productId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
                externalLegalEntityId, arrangementName, currency, Integer.valueOf(productId));

            arrangementsPostRequestBodies.add(arrangementsPostRequestBody);
        });

        return arrangementsPostRequestBodies;
    }

    private static ArrangementsPostRequestBody getArrangementsPostRequestBody(String externalLegalEntityId,
        String currentAccountName, Currency currency, int productId) {
        String accountNumber = currency == EUR
            ? generateRandomIban() : valueOf(generateRandomNumberInRange(0, 999999999));
        String bic = faker.finance().bic();
        String arrangementNameSuffix =
            " " + currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);
        String fullArrangementName = currentAccountName + arrangementNameSuffix;

        final HashSet<DebitCard> debitCards = new HashSet<>();

        if (productId == 1) {
            for (int i = 0;
                i < generateRandomNumberInRange(globalProperties.getInt(PROPERTY_DEBIT_CARDS_MIN),
                    globalProperties.getInt(PROPERTY_DEBIT_CARDS_MAX)); i++) {
                debitCards.add(new DebitCard()
                    .withNumber(String.valueOf(generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }
        }

        ArrangementsPostRequestBody arrangementsPostRequestBody = new ArrangementsPostRequestBody()
            .withId(UUID.randomUUID().toString())
            .withLegalEntityId(externalLegalEntityId)
            .withProductId(String.valueOf(productId))
            .withName(fullArrangementName)
            .withAlias(faker.lorem().characters(10))
            .withBookedBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withAvailableBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withCreditLimit(generateRandomAmountInRange(10000L, 999999L))
            .withCurrency(currency)
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(true)
            .withAccruedInterest(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(10)))
            .withNumber(String.format("%s", ThreadLocalRandom.current().nextInt(9999)))
            .withPrincipalAmount(generateRandomAmountInRange(10000L, 999999L))
            .withCurrentInvestmentValue(generateRandomAmountInRange(10000L, 999999L))
            .withDebitAccount(true)
            .withCreditAccount(true)
            .withDebitCards(debitCards)
            .withAccountHolderName(faker.name().fullName())
            .withAccountHolderAddressLine1(faker.address().streetAddress())
            .withAccountHolderAddressLine2(faker.address().secondaryAddress())
            .withAccountHolderStreetName(faker.address().streetAddress())
            .withPostCode(faker.address().zipCode())
            .withTown(faker.address().city())
            .withAccountHolderCountry(getRandomFromEnumValues(AccountHolderCountry.values()))
            .withCountrySubDivision(faker.address().state())
            .withBIC(bic);

        if (currency.equals(EUR)) {
            arrangementsPostRequestBody
                .withIBAN(accountNumber)
                .withBBAN(accountNumber.substring(3).replaceAll("[a-zA-Z]", ""));
        } else {
            arrangementsPostRequestBody
                .withBBAN(accountNumber);
        }
        return arrangementsPostRequestBody;
    }

    static ArrangementsPostRequestBody generateArrangementsPostRequestBody(String externalLegalEntityId,
        int productId, Currency currency, ArrangementType arrangementType, String arrangementName) {
        AccountHolderCountry[] accountHolderCountries = AccountHolderCountry.values();
        boolean debitCreditAccountIndicator = productId == 1 || productId == 2;
        final HashSet<DebitCard> debitCards = new HashSet<>();

        if (productId == 1) {
            for (int i = 0;
                i < generateRandomNumberInRange(globalProperties.getInt(PROPERTY_DEBIT_CARDS_MIN),
                    globalProperties.getInt(PROPERTY_DEBIT_CARDS_MAX)); i++) {
                debitCards.add(new DebitCard()
                    .withNumber(String.valueOf(generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }
        }

        String currentAccountArrangementName = arrangementType == INTERNATIONAL_TRADE
            ? INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.get(currency)
            : CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(arrangementType)
                .get(ThreadLocalRandom.current().nextInt(
                    CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(arrangementType).size()));

        String accountNumber = currency == EUR
            ? generateRandomIban() : valueOf(generateRandomNumberInRange(0, 999999999));

        String bic = faker.finance().bic();

        String arrangementNameSuffix =
            " " + currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);

        String fullArrangementName = arrangementName != null ? arrangementName + arrangementNameSuffix :
            constructArrangementName(productId, currentAccountArrangementName, arrangementNameSuffix);
        String id = UUID.randomUUID().toString();
        LOGGER.debug("generateArrangementsPostRequestBody id: {}, ccy: {} leId {} productId {}",
            id, currency, externalLegalEntityId, productId);

        ArrangementsPostRequestBody arrangementsPostRequestBody = new ArrangementsPostRequestBody()
            .withId(id)
            .withLegalEntityId(externalLegalEntityId)
            .withProductId(String.valueOf(productId))
            .withName(fullArrangementName)
            .withAlias(faker.lorem().characters(10))
            .withBookedBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withAvailableBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withCreditLimit(generateRandomAmountInRange(10000L, 999999L))
            .withCurrency(currency)
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(true)
            .withAccruedInterest(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(10)))
            .withNumber(String.format("%s", ThreadLocalRandom.current().nextInt(9999)))
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

        if (currency.equals(EUR)) {
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
