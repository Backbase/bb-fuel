package com.backbase.ct.bbfuel.client.user;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.integration.user.rest.spec.v2.users.EntitlementsAdminPostRequestBody;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserIntegrationRestClient extends AbstractRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserIntegrationRestClient.class);

    private static final String ENTITLEMENTS = globalProperties
        .getString(CommonConstants.PROPERTY_ACCESS_CONTROL_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String USER_INTEGRATION_SERVICE = "user-integration-service";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_ENTITLEMENTS_ADMIN = ENDPOINT_USERS + "/entitlementsAdmin";

    public UserIntegrationRestClient() {
        super(ENTITLEMENTS, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public void ingestEntitlementsAdminUnderLEAndLogResponse(String externalUserId, String externalLegalEntityId) {
        Response response = ingestEntitlementsAdminUnderLE(externalUserId, externalLegalEntityId);

        if (response.statusCode() == SC_BAD_REQUEST
            && response.then()
                .extract()
                .as(BadRequestException.class)
            .getMessage()
            .equals("User is already entitlements admin")) {
            LOGGER.warn("Entitlements admin [{}] already exists under legal entity [{}], skipped ingesting this entitlements admin",
                    externalUserId, externalLegalEntityId);
        } else if (response.statusCode() == SC_OK) {
            LOGGER.info("Entitlements admin [{}] ingested under legal entity [{}]",
                externalUserId, externalLegalEntityId);
        } else {
            response.then()
                .statusCode(SC_OK);
        }
    }

    public void ingestUserAndLogResponse(UsersPostRequestBody user) {
        Response response = ingestUser(user);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getMessage()
                .equals("User already exists")) {
            LOGGER.info("User [{}] already exists, skipped ingesting this user", user.getExternalId());
        } else if (response.statusCode() == SC_CREATED) {
            LOGGER.info("User [{}] ingested under legal entity [{}]",
                user.getExternalId(), user.getLegalEntityExternalId());
        } else {
            response.then()
                .statusCode(SC_CREATED);
        }
    }

    @Override
    protected String composeInitialPath() {
        return USER_INTEGRATION_SERVICE;
    }

    private Response ingestEntitlementsAdminUnderLE(String externalUserId, String externalLegalEntityId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(new EntitlementsAdminPostRequestBody()
                .withExternalId(externalUserId)
                .withLegalEntityExternalId(externalLegalEntityId))
            .post(getPath(ENDPOINT_ENTITLEMENTS_ADMIN));
    }

    private Response ingestUser(UsersPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_USERS));
    }
}
