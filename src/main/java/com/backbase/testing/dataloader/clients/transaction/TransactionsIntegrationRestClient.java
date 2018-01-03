package com.backbase.testing.dataloader.clients.transaction;

import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_TRANSACTIONS_BASE_URI;

public class TransactionsIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String TRANSACTION_INTEGRATION_SERVICE = "transaction-integration-service";
    private static final String ENDPOINT_TRANSACTIONS = "/transactions";

    public TransactionsIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_TRANSACTIONS_BASE_URI), SERVICE_VERSION);
        setInitialPath(TRANSACTION_INTEGRATION_SERVICE);
    }

    public Response ingestTransaction(TransactionsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_TRANSACTIONS));
    }
}
