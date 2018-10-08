package com.backbase.ct.bbfuel.client.user;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.UserGetResponseBody;
import org.springframework.stereotype.Component;

@Component
public class UserPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String USER_PRESENTATION_SERVICE = "user-presentation-service";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES = ENDPOINT_USERS + "/externalId/%s/legalentities";
    private static final String ENDPOINT_USER_BY_EXTERNAL_ID = ENDPOINT_USERS + "/externalId/%s";

    public UserPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public LegalEntityByUserGetResponseBody retrieveLegalEntityByExternalUserId(String externalUserId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES), externalUserId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByUserGetResponseBody.class);
    }

    public UserGetResponseBody getUserByExternalId(String userExternalId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_USER_BY_EXTERNAL_ID), userExternalId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(UserGetResponseBody.class);
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + USER_PRESENTATION_SERVICE;
    }

}
