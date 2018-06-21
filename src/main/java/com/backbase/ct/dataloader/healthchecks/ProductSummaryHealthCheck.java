package com.backbase.ct.dataloader.healthchecks;

import com.backbase.ct.dataloader.clients.common.RestClient;
import com.backbase.ct.dataloader.clients.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import java.util.Arrays;
import java.util.List;

public class ProductSummaryHealthCheck {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkProductSummaryServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties
            .getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);

        if (healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = Arrays.asList(
                new ArrangementsIntegrationRestClient(),
                new ProductSummaryPresentationRestClient());

            healthCheck.checkServicesHealth(restClients);
        }
    }
}
