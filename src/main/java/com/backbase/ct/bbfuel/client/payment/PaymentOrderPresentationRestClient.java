package com.backbase.ct.bbfuel.client.payment;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentOrderPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "client-api";
    private static final String PAYMENT_ORDER_PRESENTATION_SERVICE = "payment-order-presentation-service";
    private static final String ENDPOINT_PAYMENT_ORDERS = "/payment-orders";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(PAYMENT_ORDER_PRESENTATION_SERVICE + "/" + CLIENT_API);
    }

    public Response initiatePaymentOrder(InitiatePaymentOrder body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PAYMENT_ORDERS));
    }

}
