package com.backbase.ct.bbfuel.client.pfm;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.pocket.tailor.client.v2.model.Pocket;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PocketsRestClient extends RestClient {

    public static final String X_CHANGE_PATH = "x-change-path";
    public static final String X_CHANGE_KEY = "x-change-key";
    public static final String X_CHANGE_VALUE = "x-change-value";

    private static final String API_VERSION = "v2";
    private static final String ENDPOINT_POCKETS = "/pockets";

    private final BbFuelConfiguration config;

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getPockets());
        setVersion(API_VERSION);
    }

    /**
     * Ingests a Pocket.
     * The headers are provided so they are propagated from pocket-tailor
     * to api-simulator (which is being used for (arrangementOriginationApi.createArrangement).
     *
     * @param pocketPostRequest Pocket data.
     * @param counter counter for externalArrangementId generation.
     * @return Created pocket.
     */
    public Pocket ingestPocket(PocketPostRequest pocketPostRequest, int counter) {
        String externalArrangementId = "external-arrangement-origination-" + counter;
        Map<String, String> headers = new HashMap<>();
        headers.put(X_CHANGE_PATH, "$");
        headers.put(X_CHANGE_KEY, "externalArrangementId");
        headers.put(X_CHANGE_VALUE, externalArrangementId);
        return requestSpec()
            .headers(headers)
            .contentType(ContentType.JSON)
            .body(pocketPostRequest)
            .post(getPath(ENDPOINT_POCKETS))
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .extract().as(Pocket.class);
    }

    /**
     * Ingests a Pocket.
     *
     * @param pocketPostRequest Pocket data.
     * @return Created pocket.
     */
    public Pocket ingestPocket(PocketPostRequest pocketPostRequest) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(pocketPostRequest)
            .post(getPath(ENDPOINT_POCKETS))
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .extract().as(Pocket.class);
    }
}
