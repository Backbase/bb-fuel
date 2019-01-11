package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGet;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = ENDPOINT_ACCESS_GROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccessgroup());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestServiceAgreement(ServiceAgreementPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_SERVICE_AGREEMENTS));
    }

    public Response updateServiceAgreement(String internalServiceAgreementId, ServiceAgreementPutRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, internalServiceAgreementId)));
    }

    public ServiceAgreementGet retrieveServiceAgreementByExternalId(String externalServiceAgreementId) {
        return requestSpec()
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, externalServiceAgreementId)))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGet.class);
    }

}
