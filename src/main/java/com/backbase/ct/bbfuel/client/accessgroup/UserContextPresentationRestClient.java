package com.backbase.ct.bbfuel.client.accessgroup;

import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.client.v3.model.ServiceAgreementItem;
import com.backbase.dbs.accesscontrol.client.v3.model.UserContextPost;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContextPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_CONTEXT = ENDPOINT_ACCESS_GROUPS + "/user-context";
    private static final String ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS = ENDPOINT_USER_CONTEXT + "/service-agreements";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getAccessgroup() + "/" + CLIENT_API);
    }

    public void selectContextBasedOnMasterServiceAgreement() {
        ServiceAgreementItem masterServiceAgreement = getMasterServiceAgreementForUserContext();

        postUserContext(new UserContextPost()
                .serviceAgreementId(masterServiceAgreement.getId()))
                .then()
                .statusCode(SC_NO_CONTENT);
    }

    private Response postUserContext(UserContextPost userContextPostRequestBody) {
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

    public ServiceAgreementItem getMasterServiceAgreementForUserContext() {
        ServiceAgreementItem[] serviceAgreementGetResponseBodies = getServiceAgreementsForUserContext()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementItem[].class);

        return Arrays.stream(serviceAgreementGetResponseBodies)
            .filter(ServiceAgreementItem::getIsMaster)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No master service agreement found"));
    }

}
