package com.backbase.testing.dataloader.clients.user;

import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.response.Response;

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

    public Response retrieveLegalEntityByExternalUserId(String externalUserId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES), externalUserId));
    }

    public Response getUserByExternalId(String userExternalId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_USER_BY_EXTERNAL_ID), userExternalId));
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + USER_PRESENTATION_SERVICE;
    }

}
