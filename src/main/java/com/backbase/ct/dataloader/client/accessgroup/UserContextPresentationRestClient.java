package com.backbase.ct.dataloader.client.accessgroup;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.client.common.AbstractRestClient;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserContextPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_CONTEXT = ENDPOINT_ACCESS_GROUPS + "/usercontext";
    private static final String ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS = ENDPOINT_USER_CONTEXT + "/serviceagreements";
    private static final String ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID =
        ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS + "/%s/legalentities";

    public UserContextPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public void selectContextBasedOnMasterServiceAgreement() {
        ServiceAgreementGetResponseBody masterServiceAgreement = getMasterServiceAgreementForUserContext();

        String legalEntityId = getLegalEntitiesForServiceAgreements(masterServiceAgreement.getId()).get(0).getId();

        postUserContext(new UserContextPostRequestBody()
            .withServiceAgreementId(masterServiceAgreement.getId())
            .withLegalEntityId(legalEntityId))
            .then()
            .statusCode(SC_NO_CONTENT);
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + ACCESS_GROUP_PRESENTATION_SERVICE;
    }

    private Response postUserContext(UserContextPostRequestBody userContextPostRequestBody) {
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

    private Response getServiceAgreementsForUserContext() {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS));
    }

    public List<LegalEntitiesGetResponseBody> getLegalEntitiesForServiceAgreements(String serviceAgreementId) {
        return asList(requestSpec()
            .contentType(ContentType.JSON)
            .get(String
                .format(getPath(ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID), serviceAgreementId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntitiesGetResponseBody[].class));
    }

    public ServiceAgreementGetResponseBody getMasterServiceAgreementForUserContext() {
        ServiceAgreementGetResponseBody[] serviceAgreementGetResponseBodies = getServiceAgreementsForUserContext()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody[].class);

        return Arrays.stream(serviceAgreementGetResponseBodies)
            .filter(ServiceAgreementGetResponseBody::getIsMaster)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No master service agreement found"));
    }

}
