package com.backbase.testing.dataloader.clients.accessgroup;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LOCAL_ENTITLEMENTS_BASE_URI;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ServiceAgreementsIntegrationRestClient extends AbstractRestClient {

    private static final String ENTITLEMENTS = globalProperties.getString(PROPERTY_ENTITLEMENTS_BASE_URI);
    private static final String LOCAL_ENTITLEMENTS = globalProperties.getString(PROPERTY_LOCAL_ENTITLEMENTS_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_INTEGRATION_SERVICE = "accessgroup-integration-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = ENDPOINT_ACCESS_GROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";

    public ServiceAgreementsIntegrationRestClient() {
        super(USE_LOCAL ? LOCAL_ENTITLEMENTS : ENTITLEMENTS, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response ingestServiceAgreement(ServiceAgreementPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_SERVICE_AGREEMENTS));
    }

    public Response updateServiceAgreement(String internalServiceAgreementId, ServiceAgreementPutRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, internalServiceAgreementId)));
    }

    @Override
    protected String composeInitialPath() {
        return ACCESS_GROUP_INTEGRATION_SERVICE;
    }

}
