package com.backbase.testing.dataloader.clients.payment;

import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PaymentOrderPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String PAYMENT_ORDER_PRESENTATION_SERVICE = "payment-order-presentation-service";
    private static final String ENDPOINT_PAYMENT_ORDERS = "/payment-orders";

    public PaymentOrderPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(USE_LOCAL ? LOCAL_GATEWAY : GATEWAY + "/" + PAYMENT_ORDER_PRESENTATION_SERVICE);
    }

    public Response initiatePaymentOrder(InitiatePaymentOrder body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PAYMENT_ORDERS));
    }
}
