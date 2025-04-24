package com.backbase.ct.bbfuel.client.positivepay;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.positivepay.client.api.v1.model.IssuedCheckRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class PositivePayRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION ="v1";
    private static final String ENDPOINT_POSITIVE_PAY = "/checks";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getPositivePay() + "/" + CLIENT_API);
    }

    public Response submitPositivePayChecks(IssuedCheckRequest positivePayPostRequestBody) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(positivePayPostRequestBody)
                .post(getPath(ENDPOINT_POSITIVE_PAY));
    }
}

