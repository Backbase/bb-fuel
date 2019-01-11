package com.backbase.ct.bbfuel.client.limit;

import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.limit.rest.spec.v2.limits.PeriodicLimitsPostRequestBody;
import com.backbase.presentation.limit.rest.spec.v2.limits.TransactionalLimitsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LimitsPresentationRestClient extends AbstractRestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String LIMITS_PRESENTATION_SERVICE = "limits-presentation-service";
    private static final String ENDPOINT_LIMITS = "/limits";
    private static final String ENDPOINT_PERIODIC = ENDPOINT_LIMITS + "/periodic";
    private static final String ENDPOINT_TRANSACTIONAL = ENDPOINT_LIMITS + "/transactional";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response createPeriodicLimit(PeriodicLimitsPostRequestBody periodicLimitsPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(periodicLimitsPostRequestBody)
            .post(getPath(ENDPOINT_PERIODIC));
    }

    public Response createTransactionalLimit(TransactionalLimitsPostRequestBody transactionalLimitsPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(transactionalLimitsPostRequestBody)
            .post(getPath(ENDPOINT_TRANSACTIONAL));
    }

    @Override
    protected String composeInitialPath() {
        return LIMITS_PRESENTATION_SERVICE;
    }

}
