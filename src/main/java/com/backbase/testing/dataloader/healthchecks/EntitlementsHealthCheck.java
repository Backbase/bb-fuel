package com.backbase.testing.dataloader.healthchecks;

import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.testing.dataloader.clients.user.UserIntegrationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;

import java.util.Arrays;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_HEALTHCHECK_TIMEOUT_IN_MINUTES;

public class EntitlementsHealthCheck {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkEntitlementsServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties.getLong(PROPERTY_HEALTHCHECK_TIMEOUT_IN_MINUTES);

        if (healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = Arrays.asList(
                    new AccessGroupIntegrationRestClient(),
                    new AccessGroupPresentationRestClient(),
                    new ServiceAgreementsIntegrationRestClient(),
                    new LegalEntityIntegrationRestClient(),
                    new LegalEntityPresentationRestClient(),
                    new UserIntegrationRestClient(),
                    new UserPresentationRestClient());

            healthCheck.checkServicesHealth(restClients);
        }
    }
}
