package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "client-api";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = ENDPOINT_ACCESS_GROUPS + "/serviceagreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_CREATOR_ID =
        ENDPOINT_SERVICE_AGREEMENTS + "?creatorId=%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(ACCESS_GROUP_PRESENTATION_SERVICE + "/" + CLIENT_API);
    }

    public Response retrieveServiceAgreementByCreatorLegalEntityId(String internalCreatorLegalEntityId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_CREATOR_ID, internalCreatorLegalEntityId)));
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
