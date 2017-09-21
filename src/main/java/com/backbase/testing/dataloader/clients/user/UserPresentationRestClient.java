package com.backbase.testing.dataloader.clients.user;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

public class UserPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_USER_PRESENTATION_SERVICE = "/user-presentation-service/" + SERVICE_VERSION + "/users";
    private static final String ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES = ENDPOINT_USER_PRESENTATION_SERVICE + "/externalId/%s/legalentities";

    public UserPresentationRestClient() {
        super(globalProperties.get(CommonConstants.PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.get(CommonConstants.PROPERTY_GATEWAY_PATH));
    }

    public Response retrieveLegalEntityByExternalUserId(String externalUserId) {
        return requestSpec().get(String.format(ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES, externalUserId));
    }
}
