package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class ServiceAgreementsPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESSGROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESSGROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICEAGREEMENTS = ENDPOINT_ACCESSGROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICEAGREEMENTS_BY_ID = ENDPOINT_SERVICEAGREEMENTS + "/%s";

    public ServiceAgreementsPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + ACCESSGROUP_PRESENTATION_SERVICE);
    }

    public Response retrieveServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(String.format(ENDPOINT_SERVICEAGREEMENTS_BY_ID, internalServiceAgreementId)));
    }
}
