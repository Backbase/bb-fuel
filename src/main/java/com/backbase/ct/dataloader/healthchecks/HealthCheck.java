package com.backbase.ct.dataloader.healthchecks;

import com.backbase.ct.dataloader.clients.common.RestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkServicesHealth(List<RestClient> restClients) {
        long healthCheckTimeOutInMinutes = globalProperties.getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);
        long timeOutInMillis = CommonHelpers.convertMinutesToMillis(healthCheckTimeOutInMinutes);

        restClients.parallelStream()
                .forEach(restClient -> {
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < timeOutInMillis) {
                        try {
                            // Wait between retries to avoid network storm.
                            if (restClient.isUp()) {
                                LOGGER.info("[" + restClient.getInitialPath() + "] online after " + (System.currentTimeMillis() - startTime) + " milliseconds");
                                return;
                            } else {
                                LOGGER.info("[" + restClient.getInitialPath() + "] not available");
                            }
                            Thread.sleep(10000);
                        } catch (Exception ex) {
                            LOGGER.info("[" + restClient.getInitialPath() + "] not available");
                        }
                    }
                    throw new IllegalStateException("[" + restClient.getInitialPath() + "] timed out");
                });
    }
}
