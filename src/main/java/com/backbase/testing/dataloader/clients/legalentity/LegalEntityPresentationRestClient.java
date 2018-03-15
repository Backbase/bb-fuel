package com.backbase.testing.dataloader.clients.legalentity;

import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.response.Response;

public class LegalEntityPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String LEGAL_ENTITY_PRESENTATION_SERVICE = "legalentity-presentation-service";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legalentities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGAL_ENTITIES + "/external/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_MASTER = ENDPOINT_LEGAL_ENTITIES + "/%s/serviceagreements/master";


    public LegalEntityPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(getGatewayURI() + "/" + LEGAL_ENTITY_PRESENTATION_SERVICE);
    }

    public Response retrieveLegalEntityByExternalId(String externalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL), externalLegalEntityId));
    }

    public Response getMasterServiceAgreementOfLegalEntity(String internalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_SERVICE_AGREEMENTS_MASTER), internalLegalEntityId));
    }
}
