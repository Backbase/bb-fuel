package com.backbase.ct.dataloader.data;

import static com.backbase.ct.dataloader.utils.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.dataloader.utils.CommonHelpers.generateRandomNumberInRange;

import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.ct.dataloader.utils.ParserUtil;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.DebitCard;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    private static final int WEEKS_IN_A_YEAR = 52;

    static {
        List<String> allowed = Arrays
            .asList("AT", "BE", "BG", "CH", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB", "GI", "GR", "HR",
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
        ArrangementsPostRequestBodyParent.Currency currency) {
        ArrangementsPostRequestBodyParent.AccountHolderCountry[] accountHolderCountries = ArrangementsPostRequestBodyParent.AccountHolderCountry
            .values();
        int productId = CommonHelpers.generateRandomNumberInRange(1, 7);
        boolean debitCreditAccountIndicator = false;
        final HashSet<DebitCard> debitCards = new HashSet<>();

        if (productId == 1 || productId == 2) {
            debitCreditAccountIndicator = true;
        }

        if (productId == 1) {
            for (int i = 0; i < CommonHelpers
                .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_DEBIT_CARDS_MIN),
                    globalProperties.getInt(CommonConstants.PROPERTY_DEBIT_CARDS_MAX)); i++) {
                debitCards.add(new DebitCard()
                    .withNumber(String.format("%s", CommonHelpers.generateRandomNumberInRange(1111, 9999)))
                    .withExpiryDate(faker.business()
                        .creditCardExpiry()));
            }
        }

        ArrangementsPostRequestBody arrangementsPostRequestBody = new ArrangementsPostRequestBody()
            .withId(UUID.randomUUID().toString())
            .withLegalEntityId(externalLegalEntityId)
            .withProductId(String.format("%s", productId))
            .withName(faker.lorem().sentence(3, 0).replace(".", ""))
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
                accountHolderCountries[CommonHelpers.generateRandomNumberInRange(0, accountHolderCountries.length - 1)])
            .withCountrySubDivision(faker.address().state());

        if (currency.equals(ArrangementsPostRequestBodyParent.Currency.EUR)) {
            arrangementsPostRequestBody.withIBAN(generateRandomIban());
        } else {
            arrangementsPostRequestBody
                .withBBAN(String.valueOf(CommonHelpers.generateRandomNumberInRange(0, 999999999)));
        }

        return arrangementsPostRequestBody;
    }

    public static List<BalanceHistoryPostRequestBody> generateBalanceHistoryPostRequestBodies(
        String externalArrangementId) {
        List<BalanceHistoryPostRequestBody> balanceHistoryPostRequestBodies = new ArrayList<>();

        for (int i = -1; i >= -WEEKS_IN_A_YEAR; i--) {
            balanceHistoryPostRequestBodies.add(generateBalanceHistoryPostRequestBody(
                externalArrangementId, DateUtils.addWeeks(new Date(), generateRandomNumberInRange(i, 0))));
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
