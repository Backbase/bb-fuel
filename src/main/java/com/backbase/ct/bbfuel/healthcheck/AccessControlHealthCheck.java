package com.backbase.ct.bbfuel.healthcheck;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserIntegrationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.GlobalProperties;
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
