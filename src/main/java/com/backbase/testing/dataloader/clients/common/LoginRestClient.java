package com.backbase.testing.dataloader.clients.common;

import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.ValidatableResponse;

import java.util.HashMap;
import java.util.Map;

public class LoginRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String PROPERTY_LOGIN_PATH = "login.path";

    public LoginRestClient() {
        super(globalProperties.getString(CommonConstants.PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.getString(CommonConstants.PROPERTY_GATEWAY_PATH) + globalProperties.getString(PROPERTY_LOGIN_PATH));
    }

    public void login(String username, String password) {
        ValidatableResponse response = requestSpec().param("username", username)
                .param("password", password)
                .param("submit", "Login")
                .post("").then();

        response.assertThat().statusCode(200);

        Map<String, String> cookies = new HashMap<>(response.extract().cookies());
        setUpCookies(cookies);
    }
}
