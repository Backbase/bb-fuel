package com.backbase.ct.bbfuel.client.legalentity;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.ac_legalentity.integration.v3.model.LegalEntityItem;
import com.backbase.dbs.accesscontrol.ac_legalentity.integration.v3.model.SingleServiceAgreement;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalEntityIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3/access-control";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legal-entities";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_SINGLE =
        ENDPOINT_LEGAL_ENTITIES + "/%s/service-agreements/single";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getLegalentity());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestLegalEntity(LegalEntityItem body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_LEGAL_ENTITIES));
    }

    public SingleServiceAgreement getSingleServiceAgreementOfLegalEntity(String externalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_SERVICE_AGREEMENTS_SINGLE), externalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(SingleServiceAgreement.class);
    }

}
