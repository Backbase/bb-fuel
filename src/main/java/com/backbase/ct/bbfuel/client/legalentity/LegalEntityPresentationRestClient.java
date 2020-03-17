package com.backbase.ct.bbfuel.client.legalentity;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import io.restassured.response.Response;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalEntityPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "client-api";
    private static final String LEGAL_ENTITY_PRESENTATION_SERVICE = "legalentity-presentation-service";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legalentities";
    private static final String ENDPOINT_SUB_ENTITIES = ENDPOINT_LEGAL_ENTITIES + "/sub-entities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGAL_ENTITIES + "/external/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_MASTER =
        ENDPOINT_LEGAL_ENTITIES + "/%s/serviceagreements/master";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(LEGAL_ENTITY_PRESENTATION_SERVICE + "/" + CLIENT_API);
    }

    public List<LegalEntitiesGetResponseBody> retrieveLegalEntities() {
        return asList(requestSpec()
            .get(getPath(ENDPOINT_SUB_ENTITIES))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntitiesGetResponseBody[].class));
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

}
