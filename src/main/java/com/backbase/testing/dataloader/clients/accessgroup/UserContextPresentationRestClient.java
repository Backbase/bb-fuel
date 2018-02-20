package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public class UserContextPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESSGROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESSGROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_CONTEXT = ENDPOINT_ACCESSGROUPS + "/usercontext";
    private static final String ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS = ENDPOINT_USER_CONTEXT + "/serviceagreements";
    private static final String ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID = ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS + "/%s/legalentities";

    public UserContextPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + ACCESSGROUP_PRESENTATION_SERVICE);
    }

    public Response postUserContext(UserContextPostRequestBody userContextPostRequestBody) {
        Response response = requestSpec()
                .contentType(ContentType.JSON)
                .body(userContextPostRequestBody)
                .post(getPath(ENDPOINT_USER_CONTEXT));

        Map<String, String> cookies = new HashMap<>(response.then()
                .extract()
                .cookies());
        setUpCookies(cookies);

        return response;
    }

    public Response getServiceAgreementsForUserContext() {
        return requestSpec()
                .contentType(ContentType.JSON)
                .get(getPath(ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS));
    }

    public Response getLegalEntitiesForServiceAgreements(String serviceAgreementId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .get(String.format(getPath(ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID), serviceAgreementId));
    }

    public void selectContextBasedOnMasterServiceAgreement() {
        ServiceAgreementGetResponseBody masterServiceAgreement = getMasterServiceAgreementForUserContext();
        String serviceAgreementId = masterServiceAgreement != null ? masterServiceAgreement.getId() : null;

        String legalEntityId = getLegalEntitiesForServiceAgreements(serviceAgreementId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(LegalEntitiesGetResponseBody[].class)[0].getId();

        postUserContext(new UserContextPostRequestBody()
                .withServiceAgreementId(serviceAgreementId)
                .withLegalEntityId(legalEntityId))
                .then()
                .statusCode(SC_NO_CONTENT);
    }

    private ServiceAgreementGetResponseBody getMasterServiceAgreementForUserContext() {
        ServiceAgreementGetResponseBody[] serviceAgreementGetResponseBodies = getServiceAgreementsForUserContext()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody[].class);

        return Arrays.stream(serviceAgreementGetResponseBodies)
            .filter(ServiceAgreementGetResponseBody::getIsMaster)
            .findFirst()
            .orElse(null);
    }
}