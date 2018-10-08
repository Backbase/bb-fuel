package com.backbase.ct.bbfuel.healthcheck;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.client.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.GlobalProperties;
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
