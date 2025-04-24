package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.client.v3.model.ServiceAgreementItem;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = ENDPOINT_ACCESS_GROUPS + "/service-agreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getAccessgroup() + "/" + CLIENT_API);
    }

    public ServiceAgreementItem retrieveServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, internalServiceAgreementId)))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementItem.class);
    }

}
