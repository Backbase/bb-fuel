package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import com.backbase.testing.dataloader.clients.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.testing.dataloader.data.ProductSummaryDataGenerator;
import com.backbase.testing.dataloader.dto.ArrangementId;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_CREATED;

public class ProductSummaryConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSummaryConfigurator.class);

    private ProductSummaryDataGenerator productSummaryDataGenerator = new ProductSummaryDataGenerator();
    private ArrangementsIntegrationRestClient arrangementsIntegrationRestClient = new ArrangementsIntegrationRestClient();

    public void ingestProducts() throws IOException {
        ProductsPostRequestBody[] products = productSummaryDataGenerator.generateProductsPostRequestBodies();

        for (ProductsPostRequestBody product : products) {
            arrangementsIntegrationRestClient.ingestProduct(product)
                    .then()
                    .statusCode(SC_CREATED);
            LOGGER.info(String.format("Product [%s] ingested", product.getProductKindName()));
        }
    }

    public List<ArrangementId> ingestArrangementsByLegalEntityAndReturnArrangementIds(String externalLegalEntityId) {
        List<ArrangementId> arrangementIds = new ArrayList<>();

        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(10, 50); i++) {
            ArrangementsPostRequestBody arrangement = productSummaryDataGenerator.generateArrangementsPostRequestBody(externalLegalEntityId);

            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient.ingestArrangement(arrangement)
                    .then()
                    .statusCode(SC_CREATED)
                    .extract()
                    .as(ArrangementsPostResponseBody.class);

            arrangementIds.add(new ArrangementId(arrangementsPostResponseBody.getId(), arrangement.getId()));

            LOGGER.info(String.format("Arrangement [%s] ingested for product [%s] under legal entity [%s]", arrangement.getName(), arrangement.getProductId(), externalLegalEntityId));
        }
        return arrangementIds;
    }
}
