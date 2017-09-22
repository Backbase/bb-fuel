package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.testing.dataloader.clients.transaction.TransactionsIntegrationRestClient;
import com.backbase.testing.dataloader.data.TransactionsDataGenerator;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.HttpStatus.SC_CREATED;

public class TransactionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConfigurator.class);

    private TransactionsDataGenerator transactionsDataGenerator = new TransactionsDataGenerator();
    private TransactionsIntegrationRestClient transactionsIntegrationRestClient = new TransactionsIntegrationRestClient();

    public void ingestTransactionsByArrangement(String externalArrangementId) {
        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(10, 50); i++) {
            TransactionsPostRequestBody transaction = transactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId);
            transactionsIntegrationRestClient.ingestTransaction(transaction)
                    .then()
                    .statusCode(SC_CREATED);
            LOGGER.info(String.format("Transaction [%s] ingested for arrangement [%s]", transaction.getDescription(), externalArrangementId));
        }
    }
}
