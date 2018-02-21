package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;

public class ServiceAgreementsIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESSGROUP_INTEGRATION_SERVICE = "accessgroup-integration-service";
    private static final String ENDPOINT_ACCESSGROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICEAGREEMENTS = ENDPOINT_ACCESSGROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICEAGREEMENTS_BY_ID = ENDPOINT_SERVICEAGREEMENTS + "/%s";

    public ServiceAgreementsIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_ENTITLEMENTS_BASE_URI), SERVICE_VERSION);
        setInitialPath(ACCESSGROUP_INTEGRATION_SERVICE);
    }

    public Response ingestServiceAgreement(ServiceAgreementPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_SERVICEAGREEMENTS));
    }

    public Response updateServiceAgreement(String internalServiceAgreementId, ServiceAgreementPutRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(String.format(ENDPOINT_SERVICEAGREEMENTS_BY_ID, internalServiceAgreementId)));
    }
}
