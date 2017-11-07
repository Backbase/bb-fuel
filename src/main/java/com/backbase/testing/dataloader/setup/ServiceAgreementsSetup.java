package com.backbase.testing.dataloader.setup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.testing.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;

import java.io.IOException;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION;

public class ServiceAgreementsSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    public void setupCustomServiceAgreements() throws IOException {
        ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
        ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION), ServiceAgreementPostRequestBody[].class);

        for (ServiceAgreementPostRequestBody serviceAgreementGetResponseBody : serviceAgreementPostRequestBodies) {
            serviceAgreementsConfigurator.ingestServiceAgreementWithProvidersAndConsumersWithAllFunctionDataGroups(serviceAgreementGetResponseBody.getProviders(), serviceAgreementGetResponseBody.getConsumers());
        }
    }
}
