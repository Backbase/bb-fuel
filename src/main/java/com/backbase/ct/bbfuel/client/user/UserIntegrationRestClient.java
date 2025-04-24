package com.backbase.ct.bbfuel.client.user;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static com.backbase.ct.bbfuel.util.ResponseUtils.isConflictException;
import static org.apache.http.HttpStatus.SC_CREATED;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_IDENTITY_FEATURE_TOGGLE;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.user.manager.integration.api.v2.model.ImportIdentityRequest;
import com.backbase.dbs.user.manager.integration.api.v2.model.UserExternal;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_IDENTITIES = ENDPOINT_USERS + "/identities";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getUser());
        setVersion(SERVICE_VERSION);
    }

    public void ingestAdminAndLogResponse(UserExternal user) {

        Response response;

        if (this.globalProperties.getBoolean(PROPERTY_IDENTITY_FEATURE_TOGGLE)) {
            response = importUserIdentity(user);
        } else {
            response = ingestUser(user);
        }

        if (isBadRequestException(response, "User already exists") || isConflictException(response, "User already exists")) {
            log.info("User [{}] already exists, skipped ingesting this user", user.getExternalId());
        } else if (response.statusCode() == SC_CREATED) {
            log.info("User [{}] ingested under legal entity [{}]",
                user.getExternalId(), user.getLegalEntityExternalId());
        } else {
            log.info("User [{}] could not be ingested", user.getExternalId());
            response.then()
                .statusCode(SC_CREATED);
        }
    }

    public Response ingestUser(UserExternal body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_USERS));
    }

    public Response importUserIdentity(UserExternal body){

        ImportIdentityRequest importBody = new ImportIdentityRequest();

        importBody
            .withExternalId(body.getExternalId())
            .withLegalEntityExternalId(body.getLegalEntityExternalId());

        return requestSpec()
            .contentType(ContentType.JSON)
            .body(importBody)
            .post(getPath(ENDPOINT_IDENTITIES));
    }
}
