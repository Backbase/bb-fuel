package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContextPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_CONTEXT = ENDPOINT_ACCESS_GROUPS + "/usercontext";
    private static final String ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS = ENDPOINT_USER_CONTEXT + "/serviceagreements";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(ACCESS_GROUP_PRESENTATION_SERVICE);
    }

    public void selectContextBasedOnMasterServiceAgreement() {
        ServiceAgreementGetResponseBody masterServiceAgreement = getMasterServiceAgreementForUserContext();

        postUserContext(new UserContextPostRequestBody()
            .withServiceAgreementId(masterServiceAgreement.getId()))
            .then()
            .statusCode(SC_NO_CONTENT);
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
