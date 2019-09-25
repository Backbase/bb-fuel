package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.billpay.BillPayPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillPayConfigurator {
    
    private final BillPayPresentationRestClient billpayPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    
    public void ingestBillPayUser(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        Response response = billpayPresentationRestClient.enrolUser();
        if (isBadRequestException(response, "The user is already enrolled")) {
            log.info("Bill Pay user [{}] is already enrolled", externalUserId);
        } else {
            response.then().assertThat().statusCode(SC_NO_CONTENT);
            log.info("Bill Pay user [{}] ingested", externalUserId);
        }
    }
}
