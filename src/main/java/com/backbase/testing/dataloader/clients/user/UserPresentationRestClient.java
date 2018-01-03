package com.backbase.testing.dataloader.clients.user;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class UserPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String USER_PRESENTATION_SERVICE = "user-presentation-service";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES = ENDPOINT_USERS + "/externalId/%s/legalentities";
    private static final String ENDPOINT_USER_BY_EXTERNAL_ID = ENDPOINT_USERS + "/externalId/%s";

    public UserPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + USER_PRESENTATION_SERVICE);

    }

    public Response retrieveLegalEntityByExternalUserId(String externalUserId) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES), externalUserId));
    }

    public Response getUserByExternalId(String userExternalId) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_USER_BY_EXTERNAL_ID), userExternalId));
    }
}
