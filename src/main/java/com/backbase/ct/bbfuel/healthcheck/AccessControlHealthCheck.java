package com.backbase.ct.bbfuel.healthcheck;

import static java.util.Arrays.asList;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessControlHealthCheck {

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;
    private final ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient;
    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final UserIntegrationRestClient userIntegrationRestClient;
    private final UserPresentationRestClient userPresentationRestClient;

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void checkAccessControlServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties
            .getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);

        if (healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = asList(
                accessGroupIntegrationRestClient,
                accessGroupPresentationRestClient,
                serviceAgreementsIntegrationRestClient,
                legalEntityIntegrationRestClient,
                legalEntityPresentationRestClient,
                userIntegrationRestClient,
                userPresentationRestClient);

            healthCheck.checkServicesHealth(restClients);
        }
    }
}
