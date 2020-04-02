package com.backbase.ct.bbfuel.client.transaction;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionsIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "service-api";
    private static final String ENDPOINT_TRANSACTIONS = "/transactions";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getTransactions());
        setInitialPath(CLIENT_API);
        setVersion(SERVICE_VERSION);
    }

    public Response ingestTransactions(List<TransactionsPostRequestBody> transactionsPostRequestBodies) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(transactionsPostRequestBodies)
            .post(getPath(ENDPOINT_TRANSACTIONS));
    }

}
