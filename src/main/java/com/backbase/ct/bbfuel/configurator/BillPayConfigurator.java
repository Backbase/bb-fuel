package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.billpay.BillPayPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillPayConfigurator {
    
    private static final Logger log = LoggerFactory.getLogger(BillPayConfigurator.class);
    
    private final BillPayPresentationRestClient billpayPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    
    public void ingestBillPayUser(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        
        Response response = billpayPresentationRestClient.enrolUser();
        if (response.getStatusCode() == SC_BAD_REQUEST && StringUtils.equals(
                        response.getBody().as(BadRequestException.class).getErrors().get(0).getMessage(),
                        "The user is already enrolled")) {
            log.info("Bill Pay user [{}] is already enrolled", externalUserId);
        } else {
            response.then().assertThat().statusCode(SC_NO_CONTENT);
            log.info("Bill Pay user [{}] ingested", externalUserId);
        }
    }

}
