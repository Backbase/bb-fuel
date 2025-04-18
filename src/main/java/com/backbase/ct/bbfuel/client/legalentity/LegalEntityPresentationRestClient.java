package com.backbase.ct.bbfuel.client.legalentity;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.user.manager.client.api.v2.model.LegalEntity;
import com.backbase.dbs.accesscontrol.client.v3.model.ServiceAgreementItem;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalEntityPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String CLIENT_API = "client-api";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legal-entities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGAL_ENTITIES + "/external/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_MASTER =
        ENDPOINT_LEGAL_ENTITIES + "/%s/service-agreements/master";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getLegalentity() + "/" + CLIENT_API);
    }

    public LegalEntity retrieveLegalEntityByExternalId(String externalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL), externalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntity.class);
    }


    public ServiceAgreementItem getMasterServiceAgreementOfLegalEntity(String internalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_SERVICE_AGREEMENTS_MASTER), internalLegalEntityId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementItem.class);
    }

}
