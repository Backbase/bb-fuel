package com.backbase.ct.dataloader.client.legalentity;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.client.common.AbstractRestClient;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class LegalEntityPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String LEGAL_ENTITY_PRESENTATION_SERVICE = "legalentity-presentation-service";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legalentities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGAL_ENTITIES + "/external/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_MASTER =
        ENDPOINT_LEGAL_ENTITIES + "/%s/serviceagreements/master";


    public LegalEntityPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response retrieveLegalEntityByExternalId(String externalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL), externalLegalEntityId));
    }

    public LegalEntityByIdGetResponseBody retrieveLegalEntityByLegalEntityId(String internalLegalEntityId) {
        return requestSpec()
            .get(getPath(ENDPOINT_LEGAL_ENTITIES + internalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByIdGetResponseBody.class);
    }

    public ServiceAgreementGetResponseBody getMasterServiceAgreementOfLegalEntity(String internalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_SERVICE_AGREEMENTS_MASTER), internalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class);
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + LEGAL_ENTITY_PRESENTATION_SERVICE;
    }

}
