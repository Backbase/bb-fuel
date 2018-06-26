package com.backbase.ct.dataloader.healthchecks;

import com.backbase.ct.dataloader.clients.common.RestClient;
import com.backbase.ct.dataloader.clients.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransactionsHealthCheck {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkTransactionsServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties
            .getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);
        boolean ingestTransactions = globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_TRANSACTIONS);

        if (ingestTransactions && healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = Collections.singletonList(
                new TransactionsIntegrationRestClient());
            healthCheck.checkServicesHealth(restClients);
        }
    }
}
