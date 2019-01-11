package com.backbase.ct.bbfuel.client.productsummary;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArrangementsIntegrationRestClient extends AbstractRestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ARRANGEMENTS = "/arrangements";
    private static final String ENDPOINT_PRODUCTS = "/products";
    private static final String ENDPOINT_BALANCE_HISTORY = "/balance-history";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getArrangements());
        setVersion(SERVICE_VERSION);
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
            log.info("Product [{}] already exists, skipped ingesting this product", product.getProductKindName());
        } else if (response.statusCode() == SC_CREATED) {
            log.info("Product [{}] ingested", product.getProductKindName());
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
        return "";
    }

    private Response ingestProduct(ProductsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PRODUCTS));
    }
}
