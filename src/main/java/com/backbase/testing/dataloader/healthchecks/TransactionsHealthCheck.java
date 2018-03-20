package com.backbase.testing.dataloader.healthchecks;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.clients.transaction.TransactionsIntegrationRestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;

import java.util.Collections;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;

public class TransactionsHealthCheck {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkTransactionsServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties.getLong(PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);
        boolean ingestTransactions = globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS);

        if (ingestTransactions && healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = Collections.singletonList(
                    new TransactionsIntegrationRestClient());
            healthCheck.checkServicesHealth(restClients);
        }
    }
}
