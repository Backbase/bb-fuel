package com.backbase.ct.bbfuel.client.payment;

import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String PAYMENT_ORDER_PRESENTATION_SERVICE = "payment-order-presentation-service";
    private static final String ENDPOINT_PAYMENT_ORDERS = "/payment-orders";

    public PaymentOrderPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response initiatePaymentOrder(InitiatePaymentOrder body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_PAYMENT_ORDERS));
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + PAYMENT_ORDER_PRESENTATION_SERVICE;
    }

}
