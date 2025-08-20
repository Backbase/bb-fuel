package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.Action;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementAdmin;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementAdminsBatchUpdateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementCreateRequest;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementDetails;
import com.backbase.dbs.accesscontrol.ac_service_agreement.integration.v1.model.ServiceAgreementUpdateRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAgreementsIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;
    private static final String SERVICE_VERSION = "v1/access-control";
    private static final String ENDPOINT_SERVICE_AGREEMENTS = "/service-agreements";
    private static final String ENDPOINT_SERVICE_AGREEMENTS_BY_ID = ENDPOINT_SERVICE_AGREEMENTS + "/%s";
    private static final String ADD_ADMINS_IN_SA = ENDPOINT_SERVICE_AGREEMENTS + "/batch/admins";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccessgroup());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestServiceAgreement(ServiceAgreementCreateRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_SERVICE_AGREEMENTS));
    }

    public Response updateServiceAgreement(String externalServiceAgreementId, ServiceAgreementUpdateRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, externalServiceAgreementId)));
    }

    public ServiceAgreementDetails retrieveServiceAgreementByExternalId(String externalServiceAgreementId) {
        return requestSpec()
            .get(getPath(String.format(ENDPOINT_SERVICE_AGREEMENTS_BY_ID, externalServiceAgreementId)))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementDetails.class);
    }

    public Response addServiceAgreementAdminsBulk(List<ServiceAgreementAdmin> listOfUsers) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(new ServiceAgreementAdminsBatchUpdateRequest().action(Action.ADD).users(listOfUsers))
            .put(getPath(ADD_ADMINS_IN_SA));
    }
}
