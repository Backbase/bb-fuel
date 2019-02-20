package com.backbase.ct.bbfuel.client.billpay;

import org.springframework.stereotype.Component;

import com.backbase.ct.bbfuel.client.common.AbstractRestClient;

import io.restassured.response.Response;

@Component
public class BillPayPresentationRestClient extends AbstractRestClient {
    
    private static final String SERVICE_VERSION = "v2";
    private static final String BILLPAY_PRESENTATION_SERVICE = "billpay-presentation-service";
    private static final String ENDPOINT_BILLPAY = "/bill-pay";
    private static final String ENDPOINT_ENROL = ENDPOINT_BILLPAY + "/enrolment";
    
    public BillPayPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }
    
    public Response enrolUser() {
        return requestSpec()
                        .post(getPath(ENDPOINT_ENROL));
    }
    
    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + BILLPAY_PRESENTATION_SERVICE;
    }

}
