package com.backbase.ct.bbfuel.client.billpay;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;

import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BillPayPresentationRestClient extends RestClient {
    
    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_BILLPAY = "/bill-pay";
    private static final String ENDPOINT_ENROL = ENDPOINT_BILLPAY + "/enrolment";
    
    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getBillpay() + "/" + CLIENT_API);
    }
    
    public Response enrolUser() {
        return requestSpec()
            .post(getPath(ENDPOINT_ENROL));
    }
}
