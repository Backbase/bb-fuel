package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomBranchCode;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomCardProvider;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomDateInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.unmodifiableList;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.input.ProductReader;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.BalanceHistoryItem;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.CardDetails;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.IntegrationDebitCardItem;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.InterestDetails;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.PostArrangement;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.ProductItem;
import com.github.javafaker.Faker;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
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
    private static final Random random = new Random();
    private static final Faker faker = new Faker();
    private static final List<CountryCode> SEPA_COUNTRY_CODES;
    private static final int WEEKS_IN_A_QUARTER = 13;
    private static final int DAYS_IN_A_WEEK = 7;
    private static final String EUR = "EUR";
    private static final ConcurrentLinkedQueue<String> staticCurrentAccountArrangementsQueue =
        new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> staticNotCurrentAccountArrangementsQueue =
        new ConcurrentLinkedQueue<>();
    public static final String DEFAULT_POCKET_EXTERNAL_ID = "default-pocket-external-id";

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
        if (currentAccountArrangementIds != null)
        Splitter.on(',').trimResults().split(currentAccountArrangementIds)
            .forEach(staticCurrentAccountArrangementsQueue::add);

        List<String> notCurrentAccountArrangementIds = GlobalProperties.getInstance()
            .getList(CommonConstants.PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_EXTERNAL_IDS);
        staticNotCurrentAccountArrangementsQueue.addAll(notCurrentAccountArrangementIds);
    }

    static String generateRandomIban() {
        return Iban.random(getRandomFromList(SEPA_COUNTRY_CODES)).toString();
    }

    public static List<ProductItem> getProductsFromFile() {
        return productReader.load();
    }

    private static String getProductTypeNameFromProductsInputFile(String productId) {
        List<ProductItem> productsPostRequestBodies = getProductsFromFile();

        ProductItem product = productsPostRequestBodies.stream()
            .filter(productsPostRequestBody -> productId.equals(productsPostRequestBody.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No product found by id: %s", productId)));

        return product.getProductTypeName();
    }

    public static PostArrangement generateParentPocketArrangement(String externalLegalEntityId) {
        PostArrangement arrangementsPostRequestBody = getArrangementsPostRequestBody(
            Optional.of("external-arrangement-origination-1"),
            externalLegalEntityId,
            "Parent Pocket Account",
            EUR,
            "default-pocket-parent-external-id");
        arrangementsPostRequestBody.setBookedBalance(BigDecimal.ZERO);
        arrangementsPostRequestBody.setAvailableBalance(BigDecimal.ZERO);
        arrangementsPostRequestBody.setAccruedInterest(BigDecimal.ZERO);
        arrangementsPostRequestBody.setPrincipalAmount(BigDecimal.ZERO);
        return arrangementsPostRequestBody;
    }

    public static PostArrangement generateChildPocketArrangement(String externalLegalEntityId,
        String externalArrangementId, int counter) {
        String currentAccountName = "Core Pocket Account" + counter;
        PostArrangement postArrangement = getArrangementsPostRequestBody(
            Optional.of(externalArrangementId),
            externalLegalEntityId,
            currentAccountName,
            EUR,
            DEFAULT_POCKET_EXTERNAL_ID);
        postArrangement.setBookedBalance(BigDecimal.ZERO);
        postArrangement.setAvailableBalance(BigDecimal.ZERO);
        postArrangement.setAccruedInterest(BigDecimal.ZERO);
        postArrangement.setPrincipalAmount(BigDecimal.ZERO);
        return postArrangement;
    }

    public static List<PostArrangement> generateCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId, ProductGroupSeed productGroupSeed, int numberOfArrangements, int numberOfExternalAccounts) {
        List<PostArrangement> arrangementsPostRequestBodies = synchronizedList(new ArrayList<>());
        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            int remainingExternalAccountsToIngest = numberOfExternalAccounts;
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
            PostArrangement arrangementsPostRequestBody = getArrangementsPostRequestBody(
                Optional.ofNullable(staticCurrentAccountArrangementsQueue.poll()), externalLegalEntityId,
                currentAccountName, currency, "1");

            if (productGroupSeed.getHasExternalAccounts() && remainingExternalAccountsToIngest > 0) {
                arrangementsPostRequestBody
                        .withFinancialInstitutionId(ThreadLocalRandom.current().nextLong(1L,6L))
                        .withSourceId("source-1");
                remainingExternalAccountsToIngest = remainingExternalAccountsToIngest-1;
            }

            HashSet<IntegrationDebitCardItem> debitCards = new HashSet<>();

            for (int i = 0; i < productGroupSeed.getNumberOfDebitCards().getRandomNumberInRange(); i++) {
                debitCards.add(new IntegrationDebitCardItem()
                    .withNumber(String.valueOf(generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }

            arrangementsPostRequestBody.withDebitCards(debitCards);

            arrangementsPostRequestBodies.add(arrangementsPostRequestBody);
        });

        return arrangementsPostRequestBodies;
    }

    public static List<PostArrangement> generateNonCurrentAccountArrangementsPostRequestBodies(
        String externalLegalEntityId, ProductGroupSeed productGroupSeed, int numberOfArrangements,
        int numberOfExternalAccounts) {
        List<PostArrangement> arrangementsPostRequestBodies = synchronizedList(new ArrayList<>());

        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            int remainingExternalAccountsToIngest = numberOfExternalAccounts;
            String currency = getRandomFromList(productGroupSeed.getCurrencies());
            String productId = getRandomFromList(productGroupSeed.getProductIds());
            String arrangementName = getProductTypeNameFromProductsInputFile(productId);
            Optional<String> externalArrangementId =
                getNotCurrentAccountArrangementExternalId(externalLegalEntityId, productId);
            PostArrangement arrangementsPostRequestBody = getArrangementsPostRequestBody(
                externalArrangementId, externalLegalEntityId, arrangementName, currency, productId);

            if (productGroupSeed.getHasExternalAccounts() && remainingExternalAccountsToIngest > 0) {
                arrangementsPostRequestBody
                        .withFinancialInstitutionId(ThreadLocalRandom.current().nextLong(1L,6L))
                        .withSourceId("source-1");
                remainingExternalAccountsToIngest = remainingExternalAccountsToIngest-1;
            }

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

    private static PostArrangement getArrangementsPostRequestBody(Optional<String> externalArrangementId,
        String externalLegalEntityId, String currentAccountName, String currency, String productId) {
        String accountNumber = EUR.equals(currency)
            ? generateRandomIban()
            : valueOf(generateRandomNumberInRange(100000, 999999999));
        String bic = faker.finance().bic();
        String arrangementNameSuffix =
            " " + currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);
        String fullArrangementName = currentAccountName + arrangementNameSuffix;

        // Credit Limit Logic
        BigDecimal creditLimit = generateRandomAmountInRange(1000L, 10000L);
        BigDecimal creditLimitUsage = generateRandomAmountInRange(0L, creditLimit.longValue());
        BigDecimal reservedAmount = generateRandomAmountInRange(0L, creditLimit.subtract(creditLimitUsage).longValue());
        BigDecimal remainingCredit = creditLimit.subtract(creditLimitUsage).subtract(reservedAmount);
        BigDecimal bookedBalance = ("4".equals(productId) ? creditLimitUsage : generateRandomAmountInRange(10000L, 9999999L));



        PostArrangement arrangementsPostRequestBody = (PostArrangement) new PostArrangement()
            .withLegalEntityIds(Collections.singleton(externalLegalEntityId))
            .withProductId(productId)
            .withId(externalArrangementId.orElse(generateRandomIdFromProductId(productId)))
            .withName(fullArrangementName)
            .withBankAlias(fullArrangementName)
            .withBookedBalance(bookedBalance)
            .withAvailableBalance(generateRandomAmountInRange(10000L, 9999999L))
            .withCreditLimit(creditLimit)
            .withCreditLimitUsage(creditLimitUsage)
            .withRemainingCredit(remainingCredit)
            .withReservedAmount(reservedAmount)
            .withMinimumPayment(generateRandomAmountInRange(10L, 250L))
            .withMinimumPaymentDueDate(generateRandomDateInRange(LocalDate.now(), LocalDate.now().plusDays(30)).atStartOfDay().atOffset(ZoneOffset.UTC))
            .withDebitAccount(ImmutableList.of("1", "2", "4", DEFAULT_POCKET_EXTERNAL_ID).contains(productId))
            .withValidThru(generateRandomDateInRange(LocalDate.now().plusDays(365), LocalDate.now().plusDays(1825)).atStartOfDay().atOffset(ZoneOffset.UTC))
            .withCurrency(currency)
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(true)
            .withAccruedInterest(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(10)))
            .withNumber(String.format("%s", ThreadLocalRandom.current().nextInt(9999)))
            .withPrincipalAmount(generateRandomAmountInRange(10000L, 999999L))
            .withOutstandingPrincipalAmount(generateRandomAmountInRange(10000L, 999999L))
            .withCurrentInvestmentValue(generateRandomAmountInRange(10000L, 999999L))
            .withCreditAccount(ImmutableList.of("1", "2", "4", "5", DEFAULT_POCKET_EXTERNAL_ID).contains(productId))
            .withAccountHolderNames(faker.name().fullName())
            .withAccountHolderAddressLine1(faker.address().streetAddress())
            .withAccountHolderAddressLine2(faker.address().secondaryAddress())
            .withAccountHolderStreetName(faker.address().streetAddress())
            .withPostCode(faker.address().zipCode())
            .withTown(faker.address().city())
            .withAccountHolderCountry(faker.address().countryCode())
            .withCountrySubDivision(faker.address().state())
            .withBIC(bic)
            .withStateId(getRandomFromList(ARRANGEMENT_STATES))
            .withCardDetails("4".equals(productId) ? generateCardDetails() : null)
            .withInterestDetails(generateInterestDetails())
            .withReservedAmount(generateRandomAmountInRange(500L, 10000L))
            .withRemainingPeriodicTransfers(generateRandomAmountInRange(500L, 10000L))
            .withNextClosingDate(generateRandomDateInRange(LocalDate.now().minusDays(30), LocalDate.now().minusDays(1)))
            .withOverdueSince(generateRandomDateInRange(LocalDate.now().minusDays(30), LocalDate.now().minusDays(1)))
            .withBankBranchCode(generateRandomBranchCode())
            .withBankBranchCode2(generateRandomBranchCode());

        if (EUR.equals(currency) && !"4".equals(productId)) {
            arrangementsPostRequestBody
                .withIBAN(accountNumber)
                .withBBAN(accountNumber.substring(3).replaceAll("[a-zA-Z]", ""));
        } else {
            arrangementsPostRequestBody
                .withBBAN(accountNumber);
        }

        return arrangementsPostRequestBody;
    }

    public static CardDetails generateCardDetails() {
        return (CardDetails) new CardDetails()
            .withAvailableCashCredit(generateRandomAmountInRange(500L, 10000L))
            .withCardProvider(generateRandomCardProviderFromList())
            .withCashCreditLimit(generateRandomAmountInRange(1500L, 15000L))
            .withLastPaymentAmount(generateRandomAmountInRange(1L, 1000L))
            .withLastPaymentDate(generateRandomDateInRange(LocalDate.now().minusDays(30), LocalDate.now()))
            .withLatePaymentFee(generateRandomNumberInRange(1, 2) == 1 ? generateRandomNumberInRange(1, 10) + "%"
                : String.valueOf(generateRandomNumberInRange(1, 50)))
            .withPreviousStatementBalance(generateRandomAmountInRange(500L, 2000L))
            .withPreviousStatementDate(
                generateRandomDateInRange(LocalDate.now().minusDays(60), LocalDate.now().minusDays(30)))
            .withSecured(generateRandomNumberInRange(1, 2) == 1)
            .withStatementBalance(generateRandomAmountInRange(500L, 2000L));
    }

    public static InterestDetails generateInterestDetails() {
        return (InterestDetails) new InterestDetails()
            .withAnnualPercentageYield(generateRandomAmountInRange(1L, 8L))
            .withCashAdvanceInterestRate(generateRandomAmountInRange(1L, 10L))
            .withDividendWithheldYTD(generateRandomNumberInRange(1, 2) == 1 ? generateRandomNumberInRange(1, 10) + "%"
                : String.valueOf(generateRandomNumberInRange(1, 50)))
            .withLastYearAccruedInterest(generateRandomAmountInRange(50L, 150L))
            .withPenaltyInterestRate(generateRandomAmountInRange(1L, 10L));
    }

    public static List<BalanceHistoryItem> generateBalanceHistoryPostRequestBodies(
        String externalArrangementId) {
        List<BalanceHistoryItem> balanceHistoryPostRequestBodies = new ArrayList<>();

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

    private static final List<String> VALID_PROVIDERS = asList(
            "mastercard", "visa-white", "union-pay", "diners-club"
    );

    private static String generateRandomCardProviderFromList() {
        return VALID_PROVIDERS.get(generateRandomNumberInRange(0, VALID_PROVIDERS.size() - 1));
    }

    private static BalanceHistoryItem generateBalanceHistoryPostRequestBody(String
        externalArrangementId, Date updatedDate) {
        return new BalanceHistoryItem()
            .withArrangementId(externalArrangementId)
            .withBalance(generateRandomAmountInRange(1000000L, 1999999L))
            .withUpdatedDate(updatedDate.toInstant().atOffset(ZoneOffset.UTC));
    }

    // These prefixes are needed for account-mock service to identify the type of product
    // and return correct mocked response
    private static String generateRandomIdFromProductId(String productId) {
        if (productId.equals("1"))
            return "A01-" + UUID.randomUUID();

        else if (productId.equals("2")){
            return "A02-" + UUID.randomUUID();
        }

        else if (productId.equals("3")){
            return "A03-" + UUID.randomUUID();
        }

        else if (productId.equals("4")){
            return "A04-" + UUID.randomUUID();
        }

        else if (productId.equals("5")){
            return "A05-" + UUID.randomUUID();
        }

        else if (productId.equals("6")){
            return "A06-" + UUID.randomUUID();
        }

        else if (productId.equals("7")){
            return "A07-" + UUID.randomUUID();
        }

        else return UUID.randomUUID().toString();

    }
}
