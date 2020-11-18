package com.backbase.ct.bbfuel.client.legalentity;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.dbs.accesscontrol.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.dbs.accesscontrol.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalEntityPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "client-api";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legalentities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGAL_ENTITIES + "/external/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_MASTER =
        ENDPOINT_LEGAL_ENTITIES + "/%s/serviceagreements/master";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getLegalentity() + "/" + CLIENT_API);
    }

    public LegalEntityByExternalIdGetResponseBody retrieveLegalEntityByExternalId(String externalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL), externalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByExternalIdGetResponseBody.class);
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
