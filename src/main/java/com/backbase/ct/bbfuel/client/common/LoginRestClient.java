package com.backbase.ct.bbfuel.client.common;

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
        setBaseUri(config.getPlatform().getAuth());
    }

    public void loginBankAdmin() {
        String admin = legalEntityService.getRootAdmin();
        login(admin, admin);
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
