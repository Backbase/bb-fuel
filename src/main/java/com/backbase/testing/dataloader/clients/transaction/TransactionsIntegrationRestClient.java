package com.backbase.testing.dataloader.clients.transaction;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_TRANSACTIONS_BASE_URI;

public class TransactionsIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_TRANSACTION_INTEGRATION_SERVICE = "/transaction-integration-service/" + SERVICE_VERSION + "/transactions";

    public TransactionsIntegrationRestClient() {
        super(globalProperties.get(PROPERTY_TRANSACTIONS_BASE_URI));
    }

    public Response ingestTransaction(TransactionsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_TRANSACTION_INTEGRATION_SERVICE);
    }
}
