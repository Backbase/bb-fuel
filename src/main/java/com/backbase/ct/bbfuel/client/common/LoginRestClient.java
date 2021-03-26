package com.backbase.ct.bbfuel.client.common;

import static com.backbase.ct.bbfuel.data.CommonConstants.ACCESS_TOKEN;
import static com.backbase.ct.bbfuel.data.CommonConstants.IDENTITY_AUTH;
import static com.backbase.ct.bbfuel.data.CommonConstants.IDENTITY_TOKEN_PATH;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_IDENTITY_CLIENT;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_IDENTITY_FEATURE_TOGGLE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_IDENTITY_REALM;
import static com.backbase.ct.bbfuel.data.CommonConstants.REFRESH_TOKEN;
import static com.backbase.ct.bbfuel.data.CommonConstants.SESSION_TOKEN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN_PASSWORD;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.service.LegalEntityService;
import io.restassured.response.ValidatableResponse;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginRestClient extends RestClient {

    private final BbFuelConfiguration config;
    private final LegalEntityService legalEntityService;

    @PostConstruct
    public void init() {
        if (this.globalProperties.getBoolean(PROPERTY_IDENTITY_FEATURE_TOGGLE)) {
            setBaseUri(config.getPlatform().getIdentity());
        } else {
            setBaseUri(config.getPlatform().getAuth());
        }
    }

    public void loginBankAdmin() {
        String bankAdminUsername = legalEntityService.getRootAdmin();
        String bankAdminPassword = this.globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN_PASSWORD);
        login(bankAdminUsername, bankAdminPassword);
    }

    public void login(String username, String password) {
        ValidatableResponse response;
        Map<String, String> cookies;

        if (this.globalProperties.getBoolean(PROPERTY_IDENTITY_FEATURE_TOGGLE)) {
            String path =
                IDENTITY_AUTH + "/" + this.globalProperties.getString(PROPERTY_IDENTITY_REALM) + IDENTITY_TOKEN_PATH;
            response = requestSpec()
                .param("client_id", this.globalProperties.getString(PROPERTY_IDENTITY_CLIENT))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
                .post(path)
                .then()
                .statusCode(SC_OK);

            String token = response.extract().jsonPath().get(ACCESS_TOKEN);
            String refreshToken = response.extract().jsonPath().get(REFRESH_TOKEN);
            String csrfToken = response.extract().jsonPath().get(SESSION_TOKEN);

            cookies = new HashMap<>(response.extract().cookies());
            cookies.put("Authorization", token);
            cookies.put("refresh_token", refreshToken);
            cookies.put("XSRF-TOKEN", csrfToken);

        } else {
            response = requestSpec().param("username", username)
                .param("password", password)
                .param("submit", "Login")
                .post("")
                .then()
                .statusCode(SC_OK);

            cookies = new HashMap<>(response.extract().cookies());
        }
        setUpCookies(cookies);
    }

}
