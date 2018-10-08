package com.backbase.ct.bbfuel.client.transaction;

import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransactionsIntegrationRestClient extends AbstractRestClient {

    private static final String TRANSACTIONS = globalProperties
        .getString(CommonConstants.PROPERTY_TRANSACTIONS_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String TRANSACTION_INTEGRATION_SERVICE = "transaction-integration-service";
    private static final String ENDPOINT_TRANSACTIONS = "/transactions";

    public TransactionsIntegrationRestClient() {
        super(TRANSACTIONS, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response ingestTransactions(List<TransactionsPostRequestBody> transactionsPostRequestBodies) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(transactionsPostRequestBodies)
            .post(getPath(ENDPOINT_TRANSACTIONS));
    }

    @Override
    protected String composeInitialPath() {
        return TRANSACTION_INTEGRATION_SERVICE;
    }

}
