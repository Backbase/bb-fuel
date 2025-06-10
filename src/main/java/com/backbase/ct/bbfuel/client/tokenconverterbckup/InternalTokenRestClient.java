package com.backbase.ct.bbfuel.client.tokenconverterbckup;


import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InternalTokenRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/x-www-form-urlencoded";
    private static final String HARDCODED_BODY = "grant_type=client_credentials&scope=api%3Aservice&client_id=bb-client&client_secret=bb-secret";
    private static final String INTERNAL_TOKEN_POST_URL = "/oauth/token";
    private static final String INTERNAL_TOKEN_JSON_PATH = "access_token";

    public InternalTokenRestClient(BbFuelConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        setBaseUri(getProperTokenConverterServiceUri());
    }

    public String getAuthorisationHeaderForInternalRequest() {
        return requestSpec()
            .header(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE)
            .body(HARDCODED_BODY)
            .post(INTERNAL_TOKEN_POST_URL)
            .then()
            .extract()
            .body()
            .jsonPath().getString(INTERNAL_TOKEN_JSON_PATH);
    }

    private String getProperTokenConverterServiceUri() {
        String initialUri = config.getPlatform().getTokenconverter();
        boolean isCX6 = config.getPlatform().getRegistry().contains("editorial");
        return isCX6 ? initialUri.replace("8080", "8761") : initialUri;
    }
}
