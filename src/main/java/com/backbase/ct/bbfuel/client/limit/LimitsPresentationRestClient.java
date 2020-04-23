package com.backbase.ct.bbfuel.client.limit;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.limit.rest.spec.v2.limits.CreateLimitRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class LimitsPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "client-api";
    private static final String ENDPOINT_LIMITS = "/limits";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getLimits() + "/" + CLIENT_API);
    }

    public Response createTransactionalLimit(CreateLimitRequestBody transactionalLimitsPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(transactionalLimitsPostRequestBody)
            .post(getPath(ENDPOINT_LIMITS));
    }
}
