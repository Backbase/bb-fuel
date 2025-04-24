package com.backbase.ct.bbfuel.client.payment;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.paymentorder.client.api.v3.model.InitiatePaymentOrderWithId;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentOrderPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_PAYMENT_ORDERS = "/payment-orders";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getPayments() + "/" + CLIENT_API);
    }

    public Response initiatePaymentOrder(InitiatePaymentOrderWithId body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PAYMENT_ORDERS));
    }

}
