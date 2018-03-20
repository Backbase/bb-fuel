package com.backbase.ct.dataloader.clients.accessgroup;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ServiceAgreementsPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = ENDPOINT_ACCESS_GROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_CREATOR_ID = ENDPOINT_SERVICE_AGREEMENTS + "?creatorId=%s";

    public ServiceAgreementsPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response retrieveServiceAgreementByCreatorLegalEntityId(String internalCreatorLegalEntityId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_CREATOR_ID, internalCreatorLegalEntityId)));
    }

    public Response retrieveServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, internalServiceAgreementId)));
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + ACCESS_GROUP_PRESENTATION_SERVICE;
    }

}
