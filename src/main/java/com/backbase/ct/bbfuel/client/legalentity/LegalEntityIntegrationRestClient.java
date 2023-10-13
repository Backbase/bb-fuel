package com.backbase.ct.bbfuel.client.legalentity;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.client.v3.model.ServiceAgreementItem;
import com.backbase.dbs.accesscontrol.legalentity.client.v2.model.LegalEntityCreateItem;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalEntityIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legalentities";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_MASTER =
        ENDPOINT_LEGAL_ENTITIES + "/%s/serviceagreements/master";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getLegalentity());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestLegalEntity(LegalEntityCreateItem body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_LEGAL_ENTITIES));
    }

    public ServiceAgreementItem getMasterServiceAgreementOfLegalEntity(String externalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_SERVICE_AGREEMENTS_MASTER), externalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementItem.class);
    }

}
