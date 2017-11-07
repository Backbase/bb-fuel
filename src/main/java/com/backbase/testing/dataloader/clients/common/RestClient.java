package com.backbase.testing.dataloader.clients.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.internal.ResponseParserRegistrar;
import io.restassured.internal.ResponseSpecificationImpl;
import io.restassured.internal.TestSpecificationImpl;
import io.restassured.internal.log.LogRepository;
import io.restassured.specification.RequestSpecification;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.config.HttpClientConfig.httpClientConfig;

/**
 * <p>
 * Usage example:
 * <pre>
 * RestClient restClient = new RestClient(globalProperties.getString("url"))
 *                          .setInitialPath(globalProperties.getString("path"))
 *                          .setUpCookies(setUpCookies);
 * and now all requests made with this rest com.backbase.environment.clients will be made to the specified url, initial path and given setUpCookies.
 * RequestSpecification requestSpec = restClient.requestSpec();
 * </pre>
 * </p>
 * <p>
 * You can also extend the RestClient to have a rest com.backbase.environment.clients class per capability:
 * <pre>
 *  class CapabilityRestClient extends RestClient {
 *    public CapabilityRestClient() {
 *       super(globalProperties.getString("url"));
 *    }
 *  }
 * </pre>
 * then:
 * <pre>
 *  CapabilityRestClient capabilityRestClient = new CapabilityRestClient();
 *  capabilityRestClient.requestSpec();
 * </pre>
 * </p>
 */
public class RestClient {

    private static final String PARAMETER_NAME = "CONNECTION_MANAGER_TIMEOUT";
    private static final int TIMEOUT_VALUE = 10000;

    private URI baseURI = null;
    private RestAssuredConfig restAssuredConfig;
    private String initialPath = "";

    private static Map<String, String> cookiesJar = new LinkedHashMap<>();
    private final ResponseParserRegistrar responseParserRegistrar = new ResponseParserRegistrar();

    public RestClient(String baseUri) {
        setBaseUri(baseUri);
    }

    private void setBaseUri(String baseUri) {
        try {
            this.baseURI = new URI(baseUri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred while creating RestClient, 'baseUri' is incorrect", e);
        }
    }

    public Map<String, String> getCookies() {
        return cookiesJar;
    }

    public RestClient setInitialPath(String initialPath) {
        this.initialPath = initialPath;
        return this;
    }

    public static void setUpCookies(Map<String, String> cookies) {
        cookiesJar.putAll(cookies);
    }


    public URI getBaseURI() {
        return baseURI;
    }

    public RestAssuredConfig getRestAssuredConfig() {
        return restAssuredConfig;
    }

    public String getInitialPath() {
        return initialPath;
    }

    public String getBaseURIWithoutPort() {
        String baseAPI;

        URI uri;
        try {
            uri = new URIBuilder().setScheme(baseURI.getScheme()).setHost(baseURI.getHost()).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred while constructing base uri without port", e);
        }
        baseAPI = uri.toString();
        return baseAPI;
    }

    public RequestSpecification requestSpec() {
        LogRepository logRepository = new LogRepository();
        restAssuredConfig = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        ))
                .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails())
                .httpClient(httpClientConfig().setParam(PARAMETER_NAME, TIMEOUT_VALUE));


        RequestSpecification requestSpec = new TestSpecificationImpl(
                new RequestSpecificationImpl(getBaseURI().toString(),
                        getBaseURI().getPort(), getInitialPath(), RestAssured.DEFAULT_AUTH, Collections.emptyList(), null,
                        RestAssured.DEFAULT_URL_ENCODING_ENABLED, restAssuredConfig, logRepository, null),
                new ResponseSpecificationImpl(RestAssured.DEFAULT_BODY_ROOT_PATH, null, this.responseParserRegistrar,
                        restAssuredConfig, logRepository)).
                getRequestSpecification();
        requestSpec.queryParam("_csrf", getCookies().get("XSRF-TOKEN"));

        requestSpec.cookies(getCookies());

        return requestSpec;
    }
}
