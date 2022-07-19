package com.backbase.ct.bbfuel.client.eaa;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.eaa.client.v1.model.AccountAggregationFlow;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalAccountAggregatorRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v1";
    private static final String CLIENT_API = "client-api";
    private static final String AGGREGATION_FLOWS_ENDPOINT = "/aggregation/flows";
    private static final String AGGREGATION_FLOW_ENDPOINT = AGGREGATION_FLOWS_ENDPOINT + "/%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getExternalAccountAggregator() + "/" + CLIENT_API);
    }

    public AccountAggregationFlow createFlow() {
        return requestSpec()
            .post(AGGREGATION_FLOW_ENDPOINT)
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(AccountAggregationFlow.class);
    }

    public void finishFlow(AccountAggregationFlow flow) {
        requestSpec()
            .delete(String.format(getPath(AGGREGATION_FLOW_ENDPOINT), flow.getId()))
            .then()
            .statusCode(SC_NO_CONTENT);
    }
}
