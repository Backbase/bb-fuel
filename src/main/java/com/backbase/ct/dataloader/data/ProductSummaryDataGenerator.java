package com.backbase.ct.dataloader.data;

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
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class ProductSummaryDataGenerator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static Faker faker = new Faker();
    private static Random random = new Random();
    private static final List<CountryCode> COUNTRY_CODES;
    private static final int WEEKS_IN_A_QUARTER = 13;
    private static final List<String> CURRENT_ACCOUNT_ARRANGEMENT_NAMES = asList(
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
    public static final Map<Integer, List<String>> PRODUCT_ARRANGEMENT_NAME_MAP = new HashMap<>();

    static {
        PRODUCT_ARRANGEMENT_NAME_MAP.put(1, CURRENT_ACCOUNT_ARRANGEMENT_NAMES);
        PRODUCT_ARRANGEMENT_NAME_MAP.put(2, singletonList("Savings Account"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(3, singletonList("Credit Card"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(4, singletonList("Loan"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(5, singletonList("Term Deposit"));
        PRODUCT_ARRANGEMENT_NAME_MAP.put(6, singletonList("Investment Account"));
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

    public static String generateRandomIban() {
        return Iban.random(COUNTRY_CODES.get(random.nextInt(COUNTRY_CODES.size()))).toString();
    }

    public static ProductsPostRequestBody[] generateProductsPostRequestBodies() throws IOException {
        return ParserUtil
            .convertJsonToObject(globalProperties.getString(CommonConstants.PROPERTY_PRODUCTS_JSON_LOCATION),
                ProductsPostRequestBody[].class);
    }

    public static ArrangementsPostRequestBody generateArrangementsPostRequestBody(String externalLegalEntityId,
        Currency currency, int productId) {
        AccountHolderCountry[] accountHolderCountries = AccountHolderCountry.values();
        boolean debitCreditAccountIndicator = productId == 1 || productId == 2;
        final HashSet<DebitCard> debitCards = new HashSet<>();

        if (productId == 1) {
            for (int i = 0;
                i < generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_DEBIT_CARDS_MIN),
                    globalProperties.getInt(CommonConstants.PROPERTY_DEBIT_CARDS_MAX)); i++) {
                debitCards.add(new DebitCard()
                    .withNumber(String.format("%s", generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }
        }

        String accountNumber = currency.equals(Currency.EUR)
            ? generateRandomIban() : valueOf(generateRandomNumberInRange(0, 999999999));

        String bic = faker.finance().bic();

        String arrangementName = PRODUCT_ARRANGEMENT_NAME_MAP.get(productId)
            .get(random.nextInt(PRODUCT_ARRANGEMENT_NAME_MAP.get(productId).size())) + " " +
            currency + " " + bic.substring(0, 3) + accountNumber.substring(accountNumber.length() - 3);

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

    private static BalanceHistoryPostRequestBody generateBalanceHistoryPostRequestBody(String externalArrangementId,
        Date updatedDate) {
        return new BalanceHistoryPostRequestBody()
            .withArrangementId(externalArrangementId)
            .withBalance(generateRandomAmountInRange(1000000L, 1999999L))
            .withUpdatedDate(updatedDate);
    }
}
