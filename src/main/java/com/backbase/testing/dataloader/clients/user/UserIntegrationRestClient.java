package com.backbase.testing.dataloader.clients.user;

import com.backbase.integration.user.rest.spec.v2.users.EntitlementsAdminPostRequestBody;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;

public class UserIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String USER_INTEGRATION_SERVICE = "user-integration-service";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_ENTITLEMENTS_ADMIN = ENDPOINT_USERS + "/entitlementsAdmin";

    public UserIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_ENTITLEMENTS_BASE_URI), SERVICE_VERSION);
        setInitialPath(USER_INTEGRATION_SERVICE);
    }

    public Response ingestEntitlementsAdminUnderLE(String userExternalId, String legalEntityExternalId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(new EntitlementsAdminPostRequestBody()
                        .withExternalId(userExternalId)
                        .withLegalEntityExternalId(legalEntityExternalId))
                .post(getPath(ENDPOINT_ENTITLEMENTS_ADMIN));
    }

    public Response ingestUser(UsersPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_USERS));
    }
}
