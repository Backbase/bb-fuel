package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.testing.dataloader.clients.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.testing.dataloader.clients.productsummary.ProductIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.data.ProductSummaryDataGenerator;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_CREATED;

public class ProductSummaryConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSummaryConfigurator.class);

    private ProductSummaryDataGenerator productSummaryDataGenerator = new ProductSummaryDataGenerator();
    private ProductIntegrationRestClient productIntegrationRestClient = new ProductIntegrationRestClient();
    private ArrangementsIntegrationRestClient arrangementsIntegrationRestClient = new ArrangementsIntegrationRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();

    public void ingestProducts() throws IOException {
        ProductsPostRequestBody[] products = productSummaryDataGenerator.generateProductsPostRequestBodies();

        for (ProductsPostRequestBody product : products) {
            productIntegrationRestClient.ingestProduct(product)
                    .then()
                    .statusCode(SC_CREATED);
            LOGGER.info(String.format("Product [%s] ingested", product.getProductKindName()));
        }
    }

    public List<String> ingestArrangementsByLegalEntityAndReturnInternalArrangementIds(String externalLegalEntityId) {
        List<String> internalArrangementIds = new ArrayList<>();

        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(10, 50); i++) {
            ArrangementsPostRequestBody arrangement = productSummaryDataGenerator.generateArrangementsPostRequestBody(externalLegalEntityId);

            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient.ingestArrangement(arrangement)
                    .then()
                    .statusCode(SC_CREATED)
                    .extract()
                    .as(ArrangementsPostResponseBody.class);

            internalArrangementIds.add(arrangementsPostResponseBody.getId());

            LOGGER.info(String.format("Arrangement [%s] ingested for product [%s] under legal entity [%s]", arrangement.getName(), arrangement.getProductId(), externalLegalEntityId));
        }
        return internalArrangementIds;
    }
}
