package com.backbase.testing.dataloader.clients.user;

import com.backbase.integration.user.rest.spec.v2.users.BadRequestException;
import com.backbase.integration.user.rest.spec.v2.users.EntitlementsAdminPostRequestBody;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

public class UserIntegrationRestClient extends RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserIntegrationRestClient.class);

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String USER_INTEGRATION_SERVICE = "user-integration-service";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_ENTITLEMENTS_ADMIN = ENDPOINT_USERS + "/entitlementsAdmin";

    public UserIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_ENTITLEMENTS_BASE_URI), SERVICE_VERSION);
        setInitialPath(USER_INTEGRATION_SERVICE);
    }

    public Response ingestEntitlementsAdminUnderLE(String externalUserId, String externalLegalEntityId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(new EntitlementsAdminPostRequestBody()
                        .withExternalId(externalUserId)
                        .withLegalEntityExternalId(externalLegalEntityId))
                .post(getPath(ENDPOINT_ENTITLEMENTS_ADMIN));
    }

    public void ingestEntitlementsAdminUnderLESkipIfAlreadyExists(String externalUserId, String externalLegalEntityId) {
        Response response = ingestEntitlementsAdminUnderLE(externalUserId, externalLegalEntityId);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrorCode()
                .equals("user.access.fetch.data.error.message.USER_ALREADY_ENTITLEMENTS_ADMIN")) {
            LOGGER.warn(String.format("Entitlements admin [%s] already exists under legal entity [%s], skipped ingesting this entitlements admin", externalUserId, externalLegalEntityId));
        } else if (response.statusCode() == SC_CREATED) {
            LOGGER.info(String.format("Entitlements admin [%s] ingested under legal entity [%s]", externalUserId, externalLegalEntityId));
        } else {
            response.then().statusCode(SC_CREATED);
        }
    }

    public Response ingestUser(UsersPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_USERS));
    }

    public void ingestUserSkipIfAlreadyExists(UsersPostRequestBody user) {
        Response response = ingestUser(user);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getMessage()
                .equals("User already exists")) {
            LOGGER.warn(String.format("User [%s] already exists, skipped ingesting this user", user.getExternalId()));
        } else if (response.statusCode() == SC_CREATED) {
            LOGGER.info(String.format("User [%s] ingested under legal entity [%s]", user.getExternalId(), user.getLegalEntityExternalId()));
        } else {
            response.then().statusCode(SC_CREATED);
        }
    }
}
