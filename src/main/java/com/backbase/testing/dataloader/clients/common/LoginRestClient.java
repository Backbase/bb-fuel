package com.backbase.testing.dataloader.clients.common;

import io.restassured.response.ValidatableResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginRestClient extends AbstractRestClient {

    private static final String PROPERTY_LOGIN_PATH = "login.path";

    public LoginRestClient() {
        super();
        //TODO: Check configuration. 403 returned accessing through Gateway.
        //setInitialPath(getGatewayURI() + globalProperties.getString(PROPERTY_LOGIN_PATH));
        setInitialPath(globalProperties.getString(PROPERTY_LOGIN_PATH));
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
