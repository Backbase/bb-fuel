package com.backbase.ct.bbfuel.client.billpay;


import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.billpay.client.v2.model.UserByIdPutRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillPayMockBankRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_USER = "/bill-pay/enrol-user";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getBillpay());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestAccounts(UserByIdPutRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(ENDPOINT_USER + "/" + body.getSubscriber().getSubscriberId()));
    }
}
