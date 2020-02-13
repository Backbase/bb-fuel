package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.ListUtils.synchronizedList;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.input.ProductReader;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.DebitCard;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class ProductSummaryDataGenerator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final ProductReader productReader = new ProductReader();
    private static Faker faker = new Faker();
    private static final List<CountryCode> SEPA_COUNTRY_CODES;
    private static final int WEEKS_IN_A_QUARTER = 13;
    private static final int DAYS_IN_A_WEEK = 7;
    private static final String EUR = "EUR";

    static {
        List<String> allowed = asList("AT", "BE", "BG", "CH", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB",
            "GI", "GR", "HR", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MT", "NL", "PL", "PT", "RO", "SE",
            "SI", "SK", "SM");
        SEPA_COUNTRY_CODES = new ArrayList<>();
        CountryCode[] values = CountryCode.values();
        for (CountryCode code : values) {
            if (allowed.contains(code.name())) {
                SEPA_COUNTRY_CODES.add(code);
            }
        }
    }

    static String generateRandomIban() {
        return Iban.random(getRandomFromList(SEPA_COUNTRY_CODES)).toString();
    }

    public static List<ProductsPostRequestBody> getProductsFromFile() {
        return productReader.load();
    }

    private static String getProductTypeNameFromProductsInputFile(String productId) {
        List<ProductsPostRequestBody> productsPostRequestBodies = getProductsFromFile();

        ProductsPostRequestBody product = productsPostRequestBodies.stream()
            .filter(productsPostRequestBody -> productId.equals(productsPostRequestBody.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No product found by id: %s", productId)));

        return product.getProductTypeName();
    }

    public static List<ArrangementsPostRequestBody> generateCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId, ProductGroupSeed productGroupSeed, int numberOfArrangements) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = synchronizedList(new ArrayList<>());
        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            int randomCurrentAccountIndex = ThreadLocalRandom.current().nextInt(productGroupSeed.getCurrentAccountNames().size());
            // To support specific currency - account name map such as in the International Trade product group example
            int randomCurrencyIndex = productGroupSeed.getCurrencies().size() == productGroupSeed.getCurrentAccountNames().size()
                ? randomCurrentAccountIndex : ThreadLocalRandom.current().nextInt(productGroupSeed.getCurrencies().size());

            int currentAccountNameIndex = randomNumber < productGroupSeed.getCurrentAccountNames().size() ? randomNumber
                : randomCurrentAccountIndex;
            int currencyIndex = randomNumber < productGroupSeed.getCurrencies().size() ? randomNumber
                : randomCurrencyIndex;

            String currentAccountName = productGroupSeed.getCurrentAccountNames().get(currentAccountNameIndex);
            String currency = productGroupSeed.getCurrencies().get(currencyIndex);
            ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
                externalLegalEntityId, currentAccountName, currency, 1);

            HashSet<DebitCard> debitCards = new HashSet<>();

            for (int i = 0; i < productGroupSeed.getNumberOfDebitCards().getRandomNumberInRange(); i++) {
                debitCards.add(new DebitCard()
                    .withNumber(String.valueOf(generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }

            arrangementsPostRequestBody.withDebitCards(debitCards);

            arrangementsPostRequestBodies.add(arrangementsPostRequestBody);
        });

        return arrangementsPostRequestBodies;
    }
    
    public static List<ArrangementsPostRequestBody> generateNonCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId, ProductGroupSeed productGroupSeed, int numberOfArrangements) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = synchronizedList(new ArrayList<>());

        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            String currency = getRandomFromList(productGroupSeed.getCurrencies());
            String productId = getRandomFromList(productGroupSeed.getProductIds());
            String arrangementName = getProductTypeNameFromProductsInputFile(productId);
            ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
                externalLegalEntityId, arrangementName, currency, Integer.valueOf(productId));

            arrangementsPostRequestBodies.add(arrangementsPostRequestBody);
        });

        return arrangementsPostRequestBodies;
    }

    private static ArrangementsPostRequestBody getArrangementsPostRequestBody(String externalLegalEntityId,
        String currentAccountName, String currency, int productId) {
        List<String> arrangementStateExternalIds = Arrays.asList("Active", "Closed", "Inactive", null);
        String accountNumber = EUR.equals(currency)
            ? generateRandomIban()
            : valueOf(generateRandomNumberInRange(0, 999999999));
        String bic = faker.finance().bic();
        String arrangementNameSuffix =
            " " + currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);
        String fullArrangementName = currentAccountName + arrangementNameSuffix;
        ArrangementsPostRequestBody arrangementsPostRequestBody = new ArrangementsPostRequestBody()
                .withId(UUID.randomUUID().toString())
                .withLegalEntityIds(Collections.singleton(externalLegalEntityId))
                .withProductId(String.valueOf(productId))
                .withName(fullArrangementName)
                .withBankAlias(fullArrangementName)
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
                .withDebitAccount(productId == 1 || productId == 2)
                .withCreditAccount(productId == 1 || productId == 2)
                .withAccountHolderName(faker.name().fullName())
                .withAccountHolderAddressLine1(faker.address().streetAddress())
                .withAccountHolderAddressLine2(faker.address().secondaryAddress())
                .withAccountHolderStreetName(faker.address().streetAddress())
                .withPostCode(faker.address().zipCode())
                .withTown(faker.address().city())
                .withAccountHolderCountry(faker.address().countryCode())
                .withCountrySubDivision(faker.address().state())
                .withBIC(bic)
                .withStateId(getRandomFromList(arrangementStateExternalIds));

        if (EUR.equals(currency)) {
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

        for (int i = 0; i >= -DAYS_IN_A_WEEK; i--) {
            balanceHistoryPostRequestBodies.add(generateBalanceHistoryPostRequestBody(
                externalArrangementId, DateUtils.addDays(new Date(), i)));
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
