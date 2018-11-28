package com.backbase.ct.bbfuel.healthcheck;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HealthCheck {
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkServicesHealth(List<RestClient> restClients) {
        long healthCheckTimeOutInMinutes = globalProperties
            .getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);
        long timeOutInMillis = CommonHelpers.convertMinutesToMillis(healthCheckTimeOutInMinutes);

        restClients.parallelStream()
            .forEach(restClient -> {
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < timeOutInMillis) {
                    try {
                        // Wait between retries to avoid network storm.
                        if (restClient.isUp()) {
                            log.info(
                                "[" + restClient.getInitialPath() + "] online after " + (System.currentTimeMillis()
                                    - startTime) + " milliseconds");
                            return;
                        } else {
                            log.info("[" + restClient.getInitialPath() + "] not available");
                        }
                    } catch (Exception ex) {
                        log.info("[" + restClient.getInitialPath() + "] not available");
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        log.info("Sleep cancelled", e);
                        Thread.currentThread().interrupt();
                    }
                }
                throw new IllegalStateException("[" + restClient.getInitialPath() + "] timed out");
            });
    }
}
