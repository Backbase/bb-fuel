package com.backbase.ct.bbfuel.client.productsummary;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestExceptionWithErrorKey;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.ArrangementAddedResponse;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.BalanceHistoryItem;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.PostArrangement;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.ProductItem;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.Subscription;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArrangementsIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    public static final String X_CHANGE_PATH = "x-change-path";
    public static final String X_CHANGE_KEY = "x-change-key";
    public static final String X_CHANGE_VALUE = "x-change-value";

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ARRANGEMENTS = "/arrangements";
    private static final String ENDPOINT_PRODUCTS = "/products";
    private static final String ENDPOINT_BALANCE_HISTORY = "/balance-history";
    private static final String ENDPOINT_SUBSCRIPTION = "/subscriptions";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getArrangements());
        setVersion(SERVICE_VERSION);
    }

    public ArrangementAddedResponse ingestArrangement(PostArrangement body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_ARRANGEMENTS))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(ArrangementAddedResponse.class);
    }

    public Response ingestPocketArrangement(PostArrangement body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_ARRANGEMENTS));
    }

    /**
     * This method will create a pocket arrangement, which when called
     * with the 3 specified headers, result in an arrangement with an
     * external arrangement id based on the method's argument externalArrangementId's value.
     *
     * @param body PostArrangement
     * @param externalArrangementId external arrangement id
     * @return Response
     */
    public Response ingestPocketArrangementOneToOne(PostArrangement body, String externalArrangementId) {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_CHANGE_PATH, "$");
        headers.put(X_CHANGE_KEY, "externalArrangementId");
        headers.put(X_CHANGE_VALUE, externalArrangementId);
        return requestSpec()
            .headers(headers)
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_ARRANGEMENTS));
    }

    /**
     * Ingest pocket arrangement for 1-to-many or 1-to-1 mode, when not already existing.
     *
     * @param modeOneToMany boolean for choosing ingestion method
     * @param arrangement the pocket arrangement generated for 1-to-many or 1-to-1 mode
     * @param externalArrangementId external arrangement id
     * @return ArrangementAddedResponse
     */
    public ArrangementAddedResponse ingestPocketArrangementAndLogResponse(PostArrangement arrangement,
        String externalArrangementId, boolean modeOneToMany) {
        ArrangementAddedResponse arrangementsPostResponseBody = null;
        Response response;
        if (modeOneToMany) {
            response = ingestPocketArrangement(arrangement);
        } else {
            response = ingestPocketArrangementOneToOne(arrangement, externalArrangementId);
        }
        if (isBadRequestExceptionWithErrorKey(response, "arrangements.api.alreadyExists.arrangement")) {
            log.info("Arrangement [{}] already exists, skipped ingesting this arrangement", arrangement.getProductId());
        } else {
            log.info("Arrangement [{}] ingested", arrangement.getId());
            arrangementsPostResponseBody = response.then()
                .statusCode(SC_CREATED)
                .extract()
                .as(ArrangementAddedResponse.class);
        }
        return arrangementsPostResponseBody;
    }

    public void ingestProductAndLogResponse(ProductItem product) {
        Response response = ingestProduct(product);

        if (isBadRequestExceptionWithErrorKey(response, "account.api.product.alreadyExists")) {
            log.info("Product [{}] already exists, skipped ingesting this product", product.getProductKindName());
        } else if (response.statusCode() == SC_CREATED) {
            log.info("Product [{}] ingested", product.getProductKindName());
        } else {
            response.then()
                .statusCode(SC_CREATED);
        }
    }

    public Response ingestBalance(BalanceHistoryItem balanceHistoryPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(balanceHistoryPostRequestBody)
            .post(getPath(ENDPOINT_BALANCE_HISTORY));
    }

    private Response ingestProduct(ProductItem body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PRODUCTS));
    }

    public Response postSubscriptions(String externalArrangementId, Subscription subscription) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(subscription)
            .post(getPath(ENDPOINT_ARRANGEMENTS) + "/" + externalArrangementId + ENDPOINT_SUBSCRIPTION);
    }
}
