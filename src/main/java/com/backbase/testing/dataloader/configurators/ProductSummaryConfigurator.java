package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import com.backbase.testing.dataloader.clients.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.testing.dataloader.dto.ArrangementId;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_MIN;
import static com.backbase.testing.dataloader.data.ProductSummaryDataGenerator.generateArrangementsPostRequestBody;
import static com.backbase.testing.dataloader.data.ProductSummaryDataGenerator.generateProductsPostRequestBodies;
import static org.apache.http.HttpStatus.SC_CREATED;

public class ProductSummaryConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSummaryConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private Random random = new Random();
    private ArrangementsIntegrationRestClient arrangementsIntegrationRestClient = new ArrangementsIntegrationRestClient();

    public void ingestProducts() throws IOException {
        ProductsPostRequestBody[] products = generateProductsPostRequestBodies();
        Arrays.stream(products).parallel().forEach(product -> arrangementsIntegrationRestClient.ingestProductAndLogResponse(product));
    }

    public List<ArrangementId> ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(String externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency currency) {
        List<ArrangementId> arrangementIds = Collections.synchronizedList(new ArrayList<>());

        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_MIN), globalProperties.getInt(PROPERTY_ARRANGEMENTS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            ArrangementsPostRequestBody arrangement = generateArrangementsPostRequestBody(externalLegalEntityId, currency);

            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient.ingestArrangement(arrangement)
                    .then()
                    .statusCode(SC_CREATED)
                    .extract()
                    .as(ArrangementsPostResponseBody.class);

            arrangementIds.add(new ArrangementId(arrangementsPostResponseBody.getId(), arrangement.getId()));

            LOGGER.info(String.format("Arrangement [%s] with currency [%s] ingested for product [%s] under legal entity [%s]", arrangement.getName(), currency, arrangement.getProductId(), externalLegalEntityId));
        });
        return arrangementIds;
    }

    public List<ArrangementId> ingestRandomCurrencyArrangementsByLegalEntityAndReturnArrangementIds(String externalLegalEntityId) {
        List<ArrangementId> arrangementIds = Collections.synchronizedList(new ArrayList<>());

        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_MIN), globalProperties.getInt(PROPERTY_ARRANGEMENTS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            ArrangementsPostRequestBodyParent.Currency currency = ArrangementsPostRequestBodyParent.Currency.values()[random.nextInt(ArrangementsPostRequestBodyParent.Currency.values().length)];
            ArrangementsPostRequestBody arrangement = generateArrangementsPostRequestBody(externalLegalEntityId, currency);

            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient.ingestArrangement(arrangement)
                    .then()
                    .statusCode(SC_CREATED)
                    .extract()
                    .as(ArrangementsPostResponseBody.class);

            arrangementIds.add(new ArrangementId(arrangementsPostResponseBody.getId(), arrangement.getId()));

            LOGGER.info(String.format("Arrangement [%s] with currency [%s] ingested for product [%s] under legal entity [%s]", arrangement.getName(), currency, arrangement.getProductId(), externalLegalEntityId));
        });
        return arrangementIds;
    }
}
