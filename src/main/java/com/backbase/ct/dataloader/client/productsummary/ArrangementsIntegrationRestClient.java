package com.backbase.ct.dataloader.client.productsummary;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.dataloader.client.common.AbstractRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArrangementsIntegrationRestClient extends AbstractRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrangementsIntegrationRestClient.class);

    private static final String PRODUCT_SUMMARY = globalProperties
        .getString(CommonConstants.PROPERTY_PRODUCT_SUMMARY_BASE_URI);

    private static final String SERVICE_VERSION = "v2";
    private static final String ARRANGEMENTS_INTEGRATION_SERVICE = "arrangements-integration-service";
    private static final String ENDPOINT_ARRANGEMENTS = "/arrangements";
    private static final String ENDPOINT_PRODUCTS = "/products";
    private static final String ENDPOINT_BALANCE_HISTORY = "/balance-history";

    public ArrangementsIntegrationRestClient() {
        super(PRODUCT_SUMMARY, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public ArrangementsPostResponseBody ingestArrangement(ArrangementsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_ARRANGEMENTS))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(ArrangementsPostResponseBody.class);
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
            LOGGER.info("Product [{}] already exists, skipped ingesting this product", product.getProductKindName());
        } else if (response.statusCode() == SC_CREATED) {
            LOGGER.info("Product [{}] ingested", product.getProductKindName());
        } else {
            response.then()
                .statusCode(SC_CREATED);
        }
    }

    public Response ingestBalance(BalanceHistoryPostRequestBody balanceHistoryPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(balanceHistoryPostRequestBody)
            .post(getPath(ENDPOINT_BALANCE_HISTORY));
    }

    @Override
    protected String composeInitialPath() {
        return ARRANGEMENTS_INTEGRATION_SERVICE;
    }

    private Response ingestProduct(ProductsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PRODUCTS));
    }
}
