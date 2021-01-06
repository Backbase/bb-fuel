package com.backbase.ct.bbfuel.client.accountstatement;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.dto.accountStatement.EstatementPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountStatementsIntegrationMockServiceApiClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String POST_DEL_ESTATEMENT = "/account/statements/mock";

    private static final String SERVICE_VERSION ="v2";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getAccountStatement());
        setVersion(SERVICE_VERSION);
        //setInitialPath("/" + "service-api");
    }

    public Response createAccountStatements(List<EstatementPostRequestBody> requestBody) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post(getPath(POST_DEL_ESTATEMENT));
    }
}

