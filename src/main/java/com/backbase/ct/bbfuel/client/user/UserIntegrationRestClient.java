package com.backbase.ct.bbfuel.client.user;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getUser());
        setVersion(SERVICE_VERSION);
    }

    public void ingestUserAndLogResponse(UsersPostRequestBody user) {
        Response response = ingestUser(user);

        if (response.statusCode() == SC_BAD_REQUEST
            && response.then()
            .extract()
            .as(BadRequestException.class)
            .getMessage()
            .equals("User already exists")) {
            log.info("User [{}] already exists, skipped ingesting this user", user.getExternalId());
        } else if (response.statusCode() == SC_CREATED) {
            log.info("User [{}] ingested under legal entity [{}]",
                user.getExternalId(), user.getLegalEntityExternalId());
        } else {
            response.then()
                .statusCode(SC_CREATED);
        }
    }

    private Response ingestUser(UsersPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_USERS));
    }
}
