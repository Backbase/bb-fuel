package com.backbase.ct.bbfuel.client.tokenconverter;

import static org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenConverterServiceApiClient extends RestClient {

    private static final String SERVICE_NAME = "bb-client";
    private static final String SERVICE_SECRET = "bb-secret";
    private static final String INTERNAL_TOKEN_JSON_PATH = "access_token";
    private static final String REQUEST_BODY =
        "grant_type=client_credentials&scope=api:service&client_id=%s&client_secret=%s";
    private static final String INTERNAL_TOKEN_POST_PATH = "/oauth/token";
    private final BbFuelConfiguration config;

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getTokenconverter());
    }

    public String getTokenFromTokenConverter() {
        return "Bearer %s".formatted(
            generateServiceToken()
                .then()
                .statusCode(SC_OK)
                .extract()
                .body()
                .jsonPath().getString(INTERNAL_TOKEN_JSON_PATH));
    }

    private Response generateServiceToken() {
        return requestSpec()
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
            .body(REQUEST_BODY.formatted(SERVICE_NAME, SERVICE_SECRET))
            .post(INTERNAL_TOKEN_POST_PATH);
    }

}
