package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.TransactionsDataGenerator;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static org.apache.http.HttpStatus.SC_CREATED;

public class TransactionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private TransactionsIntegrationRestClient transactionsIntegrationRestClient = new TransactionsIntegrationRestClient();

    public void ingestTransactionsByArrangement(String externalArrangementId) {
        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MIN), globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            TransactionsPostRequestBody transaction = TransactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId);
            transactionsIntegrationRestClient.ingestTransaction(transaction)
                    .then()
                    .statusCode(SC_CREATED);
            LOGGER.info("Transaction [{}] ingested for arrangement [{}]", transaction.getDescription(), externalArrangementId);
        });
    }
}
