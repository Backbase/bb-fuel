package com.backbase.ct.bbfuel.client.user;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.data.UserProfileData;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileRestClient extends RestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_CREATE_USER_PROFILE = "/user-profile";

    private final BbFuelConfiguration config;

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getUserProfileManager());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getUserProfileManager() + "/" + CLIENT_API);
    }

    public Response createUserProfile(String userId, String externalUserId) {
        return requestSpec()
            .given()
            .contentType(ContentType.JSON)
            .body(UserProfileData.getUserProfileData(userId, externalUserId))
            .post(String.format(getPath(ENDPOINT_CREATE_USER_PROFILE), externalUserId))
            .andReturn();
    }
}

