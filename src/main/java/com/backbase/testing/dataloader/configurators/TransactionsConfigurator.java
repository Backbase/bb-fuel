package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.testing.dataloader.clients.transaction.TransactionsIntegrationRestClient;
import com.backbase.testing.dataloader.data.TransactionsDataGenerator;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_TRANSACTIONS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_TRANSACTIONS_MIN;
import static org.apache.http.HttpStatus.SC_CREATED;

public class TransactionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private TransactionsDataGenerator transactionsDataGenerator = new TransactionsDataGenerator();
    private TransactionsIntegrationRestClient transactionsIntegrationRestClient = new TransactionsIntegrationRestClient();

    public void ingestTransactionsByArrangement(String externalArrangementId) {
        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_TRANSACTIONS_MIN), globalProperties.getInt(PROPERTY_TRANSACTIONS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            TransactionsPostRequestBody transaction = transactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId);
            transactionsIntegrationRestClient.ingestTransaction(transaction)
                    .then()
                    .statusCode(SC_CREATED);
            LOGGER.info(String.format("Transaction [%s] ingested for arrangement [%s]", transaction.getDescription(), externalArrangementId));
        });
    }
}
