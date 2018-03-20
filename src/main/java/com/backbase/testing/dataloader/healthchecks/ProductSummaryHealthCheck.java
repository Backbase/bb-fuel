package com.backbase.testing.dataloader.healthchecks;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.clients.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.testing.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;

import java.util.Arrays;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES;

public class ProductSummaryHealthCheck {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkProductSummaryServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties.getLong(PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);

        if (healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = Arrays.asList(
                    new ArrangementsIntegrationRestClient(),
                    new ProductSummaryPresentationRestClient());

            healthCheck.checkServicesHealth(restClients);
        }
    }
}
