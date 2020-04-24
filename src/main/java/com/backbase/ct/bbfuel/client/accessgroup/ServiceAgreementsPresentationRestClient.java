package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import io.restassured.http.ContentType;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = ENDPOINT_ACCESS_GROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getAccessgroup() + "/" + CLIENT_API);
    }

    public ServiceAgreementGetResponseBody retrieveServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, internalServiceAgreementId)))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class);
    }

}
