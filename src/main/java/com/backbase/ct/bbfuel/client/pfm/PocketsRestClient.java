package com.backbase.ct.bbfuel.client.pfm;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PocketsRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v1";
    private static final String ENDPOINT_POCKETS = "/pockets";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getPockets());
        setVersion(SERVICE_VERSION);
    }

    /**
     * Ingest Pockets.
     *
     * @param pocketPostRequest pocketPostRequest
     * @return Response
     */
    public Response ingestPocket(PocketPostRequest pocketPostRequest) {
        log.debug("Entering rest client accessing endpoint to ingest pockets [{}]", pocketPostRequest.toString());
        log.debug("Entering rest client with path [{}]", getPath(ENDPOINT_POCKETS));
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(pocketPostRequest)
            .post(getPath(ENDPOINT_POCKETS));
    }
}
