package com.backbase.ct.bbfuel.client.accountstatement;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.dto.accountStatement.EStatementPreferencesRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountStatementsPreferencesClient extends RestClient {

    private final BbFuelConfiguration config;


    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCOUNT_STATEMENT_PREFERENCES = "/account/statements/preferences/mock/internal-arrangement-id";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccountStatement());
        setVersion(SERVICE_VERSION);
    }

    public Response createAccountStatementsPreferences(List<EStatementPreferencesRequest> requests) {
        requests.forEach(request ->
            log.info("Account Statement Preference ingested for arrangementId [{}] for userId [{}]", request.getInternalArrangementId(), request.getUserId())
        );

        return requestSpec()
            .contentType(ContentType.JSON)
            .body(requests)
            .post(getPath(ENDPOINT_ACCOUNT_STATEMENT_PREFERENCES));
    }
}

