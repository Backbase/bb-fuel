package com.backbase.ct.bbfuel.client.common;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_LOG_ALL_REQUESTS_RESPONSES;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static java.util.Objects.*;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.config.MultiTenancyConfig;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.internal.ResponseParserRegistrar;
import io.restassured.internal.ResponseSpecificationImpl;
import io.restassured.internal.TestSpecificationImpl;
import io.restassured.internal.log.LogRepository;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usage example:
 * <pre>
 * RestClient restClient = new RestClient(globalProperties.getString("url"))
 *                          .setInitialPath(globalProperties.getString("path"))
 *                          .setUpCookies(setUpCookies);
 * and now all requests made with this rest com.backbase.environment.client will be made to the specified url, initial
 * path and given setUpCookies.
 * RequestSpecification requestSpec = restClient.requestSpec();
 * </pre>
 * You can also extend the RestClient to have a rest com.backbase.environment.client class per capability:
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
 */
@NoArgsConstructor
public class RestClient {

    protected static final String CLIENT_API = "client-api";
    private static final String PARAMETER_NAME = "CONNECTION_MANAGER_TIMEOUT";
    private static final int TIMEOUT_VALUE = 10000;
    private static final String PRODUCTION_SUPPORT_HEALTH_PATH = "/production-support/health";
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    private static final String SERVER_STATUS_UP = "UP";
    private static final String TENANT_HEADER_NAME = "X-TID";
    protected static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String X_XSRF_TOKEN_NAME = "X-XSRF-TOKEN";
    private static final String XSRF_TOKEN_NAME = "XSRF-TOKEN";

    @Getter
    private URI baseURI = null;
    private RestAssuredConfig restAssuredConfig;
    private String initialPath = "";
    @Setter
    private String version;

    private static Map<String, String> cookiesJar = new LinkedHashMap<>();
    private final ResponseParserRegistrar responseParserRegistrar = new ResponseParserRegistrar();

    public RestClient setInitialPath(String initialPath) {
        this.initialPath = initialPath;
        return this;
    }

    public RestAssuredConfig getRestAssuredConfig() {
        return restAssuredConfig;
    }

    public String getInitialPath() {
        return initialPath;
    }

    public String getPath(String endpoint) {
        return version + endpoint;
    }

    /**
     * Returns true when the health endpoint of the server returns 200 and status: "UP".
     *
     * @return Whether the service is up.
     */
    public boolean isUp() {
        Response health = getHealth();
        return health.statusCode() == SC_OK &&
            health.getBody().jsonPath().getString("status").equals(SERVER_STATUS_UP);
    }

    public RequestSpecification requestSpec() {
        LogRepository logRepository = new LogRepository();
        restAssuredConfig = RestAssuredConfig.config()
            .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            ))
            .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails())
            .httpClient(httpClientConfig().setParam(PARAMETER_NAME, TIMEOUT_VALUE));

        RequestSpecification requestSpec = new TestSpecificationImpl(
            new RequestSpecificationImpl(
                getBaseURI().toString(),
                getBaseURI().getPort(),
                getInitialPath(),
                RestAssured.DEFAULT_AUTH,
                Collections.emptyList(),
                null,
                RestAssured.DEFAULT_URL_ENCODING_ENABLED,
                restAssuredConfig,
                logRepository,
                null,
                true,
                false),
            new ResponseSpecificationImpl(RestAssured.DEFAULT_BODY_ROOT_PATH, null,
                this.responseParserRegistrar, restAssuredConfig, logRepository))
            .getRequestSpecification();

        setLoggingFilters(requestSpec);

//        requestSpec.queryParam("_csrf", getCookies().get("XSRF-TOKEN"));
        if (!isNull(getCookies().get(XSRF_TOKEN_NAME))) {
            requestSpec.header(X_XSRF_TOKEN_NAME, getCookies().get(XSRF_TOKEN_NAME));
        }

        requestSpec.cookies(getCookies());
        if (MultiTenancyConfig.isMultiTenancyEnvironment()) {
            requestSpec.header(TENANT_HEADER_NAME, MultiTenancyConfig.getTenantId());
        }

        return requestSpec;
    }

    protected static void setUpCookies(Map<String, String> cookies) {
        cookiesJar.putAll(cookies);
    }

    protected void setBaseUri(String baseUri) {
        try {
            this.baseURI = new URI(baseUri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred while creating RestClient, 'baseUri' is incorrect", e);
        }
    }

    private Map<String, String> getCookies() {
        return cookiesJar;
    }

    /**
     * @return Response containing information about the health of this service: https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html
     */
    private Response getHealth() {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(globalProperties.getBoolean(CommonConstants.PROPERTY_HEALTH_CHECK_USE_ACTUATOR)
                ? ACTUATOR_HEALTH_PATH
                : PRODUCTION_SUPPORT_HEALTH_PATH);
    }

    private void setLoggingFilters(RequestSpecification requestSpec) {
        boolean logAllRequestsResponses;
        try {
            logAllRequestsResponses = globalProperties.getBoolean(PROPERTY_LOG_ALL_REQUESTS_RESPONSES);
        } catch (NoSuchElementException e) {
            logAllRequestsResponses = false;
        }

        if (logAllRequestsResponses) {
            requestSpec
                .filter(new ResponseLoggingFilter())
                .filter(new RequestLoggingFilter());
        }
    }
}
