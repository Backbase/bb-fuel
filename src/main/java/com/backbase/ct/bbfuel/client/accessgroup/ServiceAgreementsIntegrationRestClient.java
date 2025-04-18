package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.GetServiceAgreement;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.ServiceAgreement;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.ServiceAgreementPut;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.UserServiceAgreementPair;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;
    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = "/service-agreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";
    private static final String ADD_ADMINS_IN_SA = ENDPOINT_SERVICE_AGREEMENTS + "/admins/add";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccessgroup());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestServiceAgreement(ServiceAgreement body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_SERVICE_AGREEMENTS));
    }

    public Response updateServiceAgreement(String internalServiceAgreementId, ServiceAgreementPut body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, internalServiceAgreementId)));
    }

    public GetServiceAgreement retrieveServiceAgreementByExternalId(String externalServiceAgreementId) {
        return requestSpec()
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, externalServiceAgreementId)))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(GetServiceAgreement.class);
    }

    public Response addServiceAgreementAdminsBulk(List<UserServiceAgreementPair> listOfUsers) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(listOfUsers)
            .post(getPath(ADD_ADMINS_IN_SA));
    }
}
