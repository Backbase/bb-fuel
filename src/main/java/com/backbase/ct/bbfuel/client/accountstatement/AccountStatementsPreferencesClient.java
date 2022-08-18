package com.backbase.ct.bbfuel.client.accountstatement;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.dto.accountStatement.EStatementPreferencesRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountStatementsPreferencesClient extends RestClient {

    private final BbFuelConfiguration config;


    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCOUNT_STATEMENT_PREFERENCES = "/service-api/v2/account/statements/preferences/mock";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccountStatement());
        setVersion(SERVICE_VERSION);
    }

    public Response createAccountStatementsPreferences(List<EStatementPreferencesRequest> request) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(request)
            .post(getPath(ENDPOINT_ACCOUNT_STATEMENT_PREFERENCES));
    }
}

