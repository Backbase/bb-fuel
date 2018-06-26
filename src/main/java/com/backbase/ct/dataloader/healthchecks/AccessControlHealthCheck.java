package com.backbase.ct.dataloader.healthchecks;

import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.dataloader.clients.common.RestClient;
import com.backbase.ct.dataloader.clients.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.clients.user.UserIntegrationRestClient;
import com.backbase.ct.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AccessControlHealthCheck {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkAccessControlServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties
            .getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);

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
