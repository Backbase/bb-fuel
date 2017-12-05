package com.backbase.testing.dataloader.clients.payment;

import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PaymentOrderPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_PAYMENT_ORDER_PRESENTATION_SERVICE = "/payment-order-presentation-service/" + SERVICE_VERSION + "/payment-orders";

    public PaymentOrderPresentationRestClient() {
        super(globalProperties.getString(CommonConstants.PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.getString(CommonConstants.PROPERTY_GATEWAY_PATH));
    }

    public Response initiatePaymentOrder(InitiatePaymentOrder body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_PAYMENT_ORDER_PRESENTATION_SERVICE);
    }
}
