package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.unmodifiableList;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.input.ProductReader;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.arrangements.DebitCard;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.products.ProductsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.github.javafaker.Faker;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class ProductSummaryDataGenerator {

    // initial product summary default states plus null to comply with the optional part of it
    private static final List<String> ARRANGEMENT_STATES = unmodifiableList(
        asList("Active", "Closed", "Inactive", null));
    private static final ProductReader productReader = new ProductReader();
    private static final Faker faker = new Faker();
    private static final List<CountryCode> SEPA_COUNTRY_CODES;
    private static final int WEEKS_IN_A_QUARTER = 13;
    private static final int DAYS_IN_A_WEEK = 7;
    private static final String EUR = "EUR";
    private static final ConcurrentLinkedQueue<String> staticCurrentAccountArrangementsQueue =
        new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> staticNotCurrentAccountArrangementsQueue =
        new ConcurrentLinkedQueue<>();

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

        String currentAccountArrangementIds = GlobalProperties.getInstance()
            .getString(CommonConstants.PROPERTY_ARRANGEMENT_CURRENT_ACCOUNT_EXTERNAL_IDS);
        Splitter.on(',').trimResults().split(currentAccountArrangementIds)
            .forEach(staticCurrentAccountArrangementsQueue::add);

        String notCurrentAccountArrangementIds = GlobalProperties.getInstance()
            .getString(CommonConstants.PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_EXTERNAL_IDS);
        if (notCurrentAccountArrangementIds != null) {
            Splitter.on(',').trimResults().split(notCurrentAccountArrangementIds)
                .forEach(staticNotCurrentAccountArrangementsQueue::add);
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

    public static ArrangementsPostRequestBody generateParentPocketArrangement(String externalLegalEntityId) {
        ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
            Optional.of("external-arrangement-origination-1"), externalLegalEntityId, "Parent Pocket Account", EUR, "default-pocket-parent-external-id");
        arrangementsPostRequestBody.setBookedBalance(BigDecimal.ZERO);
        arrangementsPostRequestBody.setAvailableBalance(BigDecimal.ZERO);
        arrangementsPostRequestBody.setAccruedInterest(BigDecimal.ZERO);
        arrangementsPostRequestBody.setPrincipalAmount(BigDecimal.ZERO);
        return arrangementsPostRequestBody;
    }

    public static List<ArrangementsPostRequestBody> generateCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId, ProductGroupSeed productGroupSeed, int numberOfArrangements) {
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = synchronizedList(new ArrayList<>());
        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            int randomCurrentAccountIndex = ThreadLocalRandom.current()
                .nextInt(productGroupSeed.getCurrentAccountNames().size());
            // To support specific currency - account name map such as in the International Trade product group example
            int randomCurrencyIndex =
                productGroupSeed.getCurrencies().size() == productGroupSeed.getCurrentAccountNames().size()
                    ? randomCurrentAccountIndex
                    : ThreadLocalRandom.current().nextInt(productGroupSeed.getCurrencies().size());

            int currentAccountNameIndex = randomNumber < productGroupSeed.getCurrentAccountNames().size() ? randomNumber
                : randomCurrentAccountIndex;
            int currencyIndex = randomNumber < productGroupSeed.getCurrencies().size() ? randomNumber
                : randomCurrencyIndex;

            String currentAccountName = productGroupSeed.getCurrentAccountNames().get(currentAccountNameIndex);
            String currency = productGroupSeed.getCurrencies().get(currencyIndex);
            ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
                Optional.ofNullable(staticCurrentAccountArrangementsQueue.poll()), externalLegalEntityId,
                currentAccountName, currency, "1");

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
            Optional<String> externalArrangementId =
                getNotCurrentAccountArrangementExternalId(externalLegalEntityId, productId);
            ArrangementsPostRequestBody arrangementsPostRequestBody = getArrangementsPostRequestBody(
                externalArrangementId, externalLegalEntityId, arrangementName, currency, productId);

            arrangementsPostRequestBodies.add(arrangementsPostRequestBody);
        });

        return arrangementsPostRequestBodies;
    }

    private static Optional<String> getNotCurrentAccountArrangementExternalId(String externalLegalEntityId,
        String productId) {

        if (isIdSuitableForStaticNotCurrentAccountArrangementExternalId(externalLegalEntityId)
            && isProductIdSuitableForStaticNotCurrentAccountArrangementExternalId(productId)) {
            return Optional.ofNullable(staticNotCurrentAccountArrangementsQueue.poll());
        } else {
            return Optional.empty();
        }
    }

    private static boolean isIdSuitableForStaticNotCurrentAccountArrangementExternalId(String externalLegalEntityId) {
        String limitByLegalEntityExternalId = GlobalProperties.getInstance()
            .getString(CommonConstants.PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_LEGAL_ENTITY_EXTERNAL_ID_LIMIT);
        return (limitByLegalEntityExternalId == null || externalLegalEntityId.equals(limitByLegalEntityExternalId));
    }

    private static boolean isProductIdSuitableForStaticNotCurrentAccountArrangementExternalId(String productId) {
        String limitByProductId = GlobalProperties.getInstance()
            .getString(CommonConstants.PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_PRODUCT_ID_LIMIT);
        return (limitByProductId == null || productId.equals(limitByProductId));
    }

    private static ArrangementsPostRequestBody getArrangementsPostRequestBody(Optional<String> externalArrangementId,
        String externalLegalEntityId, String currentAccountName, String currency, String productId) {
        String accountNumber = EUR.equals(currency)
            ? generateRandomIban()
            : valueOf(generateRandomNumberInRange(100000, 999999999));
        String bic = faker.finance().bic();
        String arrangementNameSuffix =
            " " + currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);
        String fullArrangementName = currentAccountName + arrangementNameSuffix;
        ArrangementsPostRequestBody arrangementsPostRequestBody = new ArrangementsPostRequestBody()
            .withId(externalArrangementId.orElse(UUID.randomUUID().toString()))
            .withLegalEntityIds(Collections.singleton(externalLegalEntityId))
            .withProductId(productId)
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
            .withDebitAccount(ImmutableList.of("1", "2", "default-pocket-external-id").contains(productId))
            .withCreditAccount(ImmutableList.of("1", "2", "4", "5", "default-pocket-external-id").contains(productId))
            .withAccountHolderNames(faker.name().fullName())
            .withAccountHolderAddressLine1(faker.address().streetAddress())
            .withAccountHolderAddressLine2(faker.address().secondaryAddress())
            .withAccountHolderStreetName(faker.address().streetAddress())
            .withPostCode(faker.address().zipCode())
            .withTown(faker.address().city())
            .withAccountHolderCountry(faker.address().countryCode())
            .withCountrySubDivision(faker.address().state())
            .withBIC(bic)
            .withStateId(getRandomFromList(ARRANGEMENT_STATES));

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
        externalArrangementId, Date updatedDate) {
        return new BalanceHistoryPostRequestBody()
            .withArrangementId(externalArrangementId)
            .withBalance(generateRandomAmountInRange(1000000L, 1999999L))
            .withUpdatedDate(updatedDate);
    }
}
