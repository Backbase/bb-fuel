package com.backbase.testing.dataloader.clients.common;

import io.restassured.response.ValidatableResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginRestClient extends AbstractRestClient {

    private static final String PROPERTY_LOGIN_PATH = "login.path";
    private static final String PROPERTY_LOCAL_LOGIN_PATH = "local.login.path";
    private static final String LOGIN = globalProperties.getString(PROPERTY_LOGIN_PATH);
    private static final String LOCAL_LOGIN = globalProperties.getString(PROPERTY_LOCAL_LOGIN_PATH);

    public LoginRestClient() {
        super();
        //TODO: Check configuration. 403 returned accessing through Gateway.
        //setInitialPath(getGatewayURI() + composeInitialPath());
        setInitialPath(composeInitialPath());
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

    @Override
    protected String composeInitialPath() {
        return USE_LOCAL ? LOCAL_LOGIN : getGatewayURI() + LOGIN;
    }

}
