package com.backbase.ct.bbfuel.client.user;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static com.backbase.ct.bbfuel.util.ResponseUtils.isConflictException;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;

import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import com.backbase.dbs.user.manager.models.v2.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_USERS = "/users";
    private static final String ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES = ENDPOINT_USERS + "/externalids/%s/legalentities";
    private static final String ENDPOINT_USER_BY_EXTERNAL_ID = ENDPOINT_USERS + "/externalids/%s";
    private static final String ENDPOINT_IDENTITIES = ENDPOINT_USERS + "/identities";

    private static final String EMAIL_DOMAIN = "@email.invalid";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getUser() + "/" + CLIENT_API);
    }

    public LegalEntityBase retrieveLegalEntityByExternalUserId(String externalUserId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_EXTERNAL_ID_LEGAL_ENTITIES), externalUserId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityBase.class);
    }

    public GetUser getUserByExternalId(String userExternalId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_USER_BY_EXTERNAL_ID), userExternalId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(GetUser.class);
    }

    public void createIdentityUserAndLogResponse(UserExternal user, String LegalEntityId) {

        Response response = createIdentity(user, LegalEntityId);

        if (isBadRequestException(response, "User already exists") || (isConflictException(response, "User already exists"))) {
            log.info("User [{}] already exists, skipped ingesting this user", user.getExternalId());
        } else if (response.statusCode() == SC_CREATED) {
            log.info("Identity User [{}] ingested under legal entity [{}]",
                user.getExternalId(), user.getLegalEntityExternalId());
        } else {
            log.info("User [{}] could not be ingested", user.getExternalId());
            response.then()
                .statusCode(SC_CREATED);
        }
    }

    private Response createIdentity(UserExternal user, String legalEntityId) {

        ImportIdentityRequest createUserBody = new ImportIdentityRequest();

        createUserBody
            .withExternalId(user.getExternalId())
            .withLegalEntityExternalId(legalEntityId);

        return requestSpec()
            .contentType(ContentType.JSON)
            .body(createUserBody)
            .post(getPath(ENDPOINT_IDENTITIES));
    }
}
