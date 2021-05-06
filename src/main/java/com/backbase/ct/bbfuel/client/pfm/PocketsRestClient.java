package com.backbase.ct.bbfuel.client.pfm;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.pocket.tailor.client.v2.model.Pocket;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import io.restassured.http.ContentType;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PocketsRestClient extends RestClient {

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
