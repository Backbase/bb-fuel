package com.backbase.ct.dataloader.healthcheck;

import com.backbase.ct.dataloader.client.common.RestClient;
import com.backbase.ct.dataloader.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.dataloader.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.util.GlobalProperties;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
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
