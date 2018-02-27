package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PRODUCTSUMMARY_BASE_URI;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

public class ArrangementsIntegrationRestClient extends RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrangementsIntegrationRestClient.class);

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ARRANGEMENTS_INTEGRATION_SERVICE = "arrangements-integration-service";
    private static final String ENDPOINT_ARRANGEMENTS = "/arrangements";
    private static final String ENDPOINT_PRODUCTS = "/products";

    public ArrangementsIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_PRODUCTSUMMARY_BASE_URI), SERVICE_VERSION);
        setInitialPath(ARRANGEMENTS_INTEGRATION_SERVICE);
    }

    public Response ingestArrangement(ArrangementsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_ARRANGEMENTS));
    }

    public Response ingestProduct(ProductsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PRODUCTS));
    }

    public void ingestProductAndLogResponse(ProductsPostRequestBody product) {
        Response response = ingestProduct(product);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrors()
                .get(0)
                .getKey()
                .equals("account.api.product.alreadyExists")) {
            LOGGER.info(String.format("Product [%s] already exists, skipped ingesting this product", product.getProductKindName()));
        } else if (response.statusCode() == SC_CREATED) {
            LOGGER.info(String.format("Product [%s] ingested", product.getProductKindName()));
        } else {
            response.then()
                .statusCode(SC_CREATED);
        }
    }
}
