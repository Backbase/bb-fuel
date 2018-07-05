package com.backbase.ct.dataloader.client.common;

import com.backbase.ct.dataloader.data.CommonConstants;
import io.restassured.response.ValidatableResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LoginRestClient extends AbstractRestClient {

    private static final String LOGIN = globalProperties.getString(CommonConstants.PROPERTY_LOGIN_PATH);

    public LoginRestClient() {
        super();
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
        return USE_LOCAL ? LOGIN : getGatewayURI() + LOGIN;
    }

}
