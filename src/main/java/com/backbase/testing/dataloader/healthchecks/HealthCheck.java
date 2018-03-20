package com.backbase.testing.dataloader.healthchecks;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES;
import static com.backbase.testing.dataloader.utils.CommonHelpers.convertMinutesToMillis;

public class HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkServicesHealth(List<RestClient> restClients) {
        long healthCheckTimeOutInMinutes = globalProperties.getLong(PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);
        long timeOutInMillis = convertMinutesToMillis(healthCheckTimeOutInMinutes);

        restClients.parallelStream()
                .forEach(restClient -> {
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < timeOutInMillis) {
                        try {
                            // Wait between retries to avoid network storm.
                            Thread.sleep(10000);
                            if (restClient.isUp()) {
                                LOGGER.info("[" + restClient.getInitialPath() + "] online after " + (System.currentTimeMillis() - startTime) + " milliseconds");
                                return;
                            } else {
                                LOGGER.info("[" + restClient.getInitialPath() + "] not available");
                            }
                        } catch (Exception ex) {
                            LOGGER.info("[" + restClient.getInitialPath() + "] not available");
                        }
                    }
                    throw new IllegalStateException("[" + restClient.getInitialPath() + "] timed out");
                });
    }
}
