package com.backbase.testing.dataloader.data;

import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.ParserUtil;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent;
import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import com.github.javafaker.Faker;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.backbase.testing.dataloader.data.CommonConstants.PRODUCTS_JSON;

public class ProductSummaryDataGenerator {

    private Faker faker = new Faker();
    private Random random = new Random();
    private static final List<CountryCode> COUNTRY_CODES;
    static {
        List<String> allowed = Arrays.asList("AT", "BE", "BG", "CH", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB", "GI", "GR", "HR", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SK", "SM");
        COUNTRY_CODES = new ArrayList<>();
        CountryCode[] values = CountryCode.values();
        for (CountryCode code : values) {
            if (allowed.contains(code.name())) {
                COUNTRY_CODES.add(code);
            }
        }
    }

    private String generateRandomIban() {
        return Iban.random(COUNTRY_CODES.get(random.nextInt(COUNTRY_CODES.size()))).toString();
    }

    public ProductsPostRequestBody[] generateProductsPostRequestBodies() throws IOException {
        return ParserUtil.convertJsonToObject(PRODUCTS_JSON, ProductsPostRequestBody[].class);
    }

    public ArrangementsPostRequestBody generateArrangementsPostRequestBody(String externalLegalEntityId) {
        return new ArrangementsPostRequestBody().withId(UUID.randomUUID().toString())
                .withLegalEntityId(externalLegalEntityId)
                .withProductId(String.format("%s", CommonHelpers.generateRandomNumberInRange(1, 7)))
                .withName(faker.lorem().sentence(3).replace(".", ""))
                .withAlias(faker.lorem().characters(10))
                .withBookedBalance(CommonHelpers.generateRandomAmountInRange(1000L, 99999L))
                .withAvailableBalance(CommonHelpers.generateRandomAmountInRange(1000L, 99999L))
                .withCreditLimit(CommonHelpers.generateRandomAmountInRange(1000L, 99999L))
                .withIBAN(generateRandomIban())
                .withBBAN(faker.lorem().characters(20).toUpperCase())
                .withCurrency(ArrangementsPostRequestBodyParent.Currency.EUR)
                .withExternalTransferAllowed(true)
                .withUrgentTransferAllowed(true)
                .withAccruedInterest(BigDecimal.valueOf(random.nextInt(10)))
                .withNumber((String.format("%s", random.nextInt(9999))))
                .withPrincipalAmount(CommonHelpers.generateRandomAmountInRange(1000L, 99999L))
                .withCurrentInvestmentValue(CommonHelpers.generateRandomAmountInRange(1000L, 99999L))
                .withDebitAccount(true)
                .withCreditAccount(true);
    }
}
