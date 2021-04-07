package com.backbase.ct.bbfuel.client.pfm;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.arrangement.integration.outbound.origination.v1.model.CreateArrangementRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PocketsMockArrangementRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v1";
    private static final String ENDPOINT_ARRANGEMENT_MOCK = "/arrangements";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getPocketsArrangements());
        setVersion(SERVICE_VERSION);
    }

    /**
     * Ingest pocket parent arrangement.
     *
     * @param createArrangementRequest createArrangementRequest
     * @return Response
     */
    public Response ingestPocketParentArrangement(CreateArrangementRequest createArrangementRequest) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(createArrangementRequest)
            .post(getPath(ENDPOINT_ARRANGEMENT_MOCK));
    }
}
