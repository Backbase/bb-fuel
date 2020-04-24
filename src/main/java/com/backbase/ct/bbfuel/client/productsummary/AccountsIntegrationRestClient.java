package com.backbase.ct.bbfuel.client.productsummary;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.integration.account.spec.v2.arrangements.ArrangementItem;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountsIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ARRANGEMENTS = "/arrangements";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccounts());
        setVersion(SERVICE_VERSION);
    }

    public List<ArrangementItem> getArrangements(String legalEntityId) {
        return Arrays.asList(requestSpec()
            .contentType(ContentType.JSON)
            .queryParam("legalEntityId", legalEntityId)
            .get(getPath(ENDPOINT_ARRANGEMENTS))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ArrangementItem[].class));
    }

}
