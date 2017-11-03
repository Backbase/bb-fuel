package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;

public class ServiceAgreementsIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE = "/accessgroup-integration-service/" + SERVICE_VERSION + "/accessgroups";
    private static final String ENDPOINT_SERVICEAGREEMENTS = ENDPOINT_ACCESSGROUP_INTEGRATION_SERVICE + "/serviceagreements";

    public ServiceAgreementsIntegrationRestClient() {
        super(globalProperties.get(PROPERTY_ENTITLEMENTS_BASE_URI));
    }

    public Response ingestServiceAgreement(ServiceAgreementPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_SERVICEAGREEMENTS);
    }
}
