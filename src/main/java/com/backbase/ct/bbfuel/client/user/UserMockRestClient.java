package com.backbase.ct.bbfuel.client.user;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.data.UserProfileData;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMockRestClient extends RestClient {

    private final BbFuelConfiguration config;
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ADD_USER_PROFILE_DATA = "/users/profile/mock/%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getUsermock());
        setVersion(SERVICE_VERSION);
    }

    public void addUserProfileData(String userId) {
        Response response = addPhoneAndElectronicAddressesData(userId);
        if (response.statusCode() == SC_NO_CONTENT) {
            log.info("User profile data is updated for user [{}]", userId);
        } else if (response.statusCode() == SC_CREATED) {
            log.info("User profile data is added for user [{}]", userId);
        } else {
            log.info("User profile data could not be added for user [{}]", userId);
        }
    }

    private Response addPhoneAndElectronicAddressesData(String userId) {
        return requestSpec()
            .given()
            .contentType(ContentType.JSON)
            .body(UserProfileData.getPhoneAndElectronicAddressData())
            .put(String.format(getPath(ENDPOINT_ADD_USER_PROFILE_DATA), userId))
            .andReturn();
    }
}
