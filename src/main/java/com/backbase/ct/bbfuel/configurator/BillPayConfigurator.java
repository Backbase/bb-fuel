package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.billpay.integration.enrolment.Account;
import com.backbase.billpay.integration.enrolment.Address;
import com.backbase.billpay.integration.enrolment.Subscriber;
import com.backbase.billpay.integration.rest.spec.v2.billpay.enroluser.UserByIdPutRequestBody;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.billpay.BillPayMockBankRestClient;
import com.backbase.ct.bbfuel.client.billpay.BillPayPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import io.restassured.response.Response;
import java.util.List;
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
    private final BillPayMockBankRestClient billPayMockBankRestClient;
    
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

    public void ingestBillPayAccounts(String subscriberId, List<Account> accounts) {
        loginRestClient.login(subscriberId, subscriberId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        UserByIdPutRequestBody request = new UserByIdPutRequestBody()
            .withSubscriber(createSubscriber(subscriberId, accounts));
        Response response = billPayMockBankRestClient.ingestAccounts(request);
        if (isBadRequestException(response, "The request is invalid")) {
            log.info("Bad request for Bill Pay user [{}]", subscriberId);
        } else {
            response.then().assertThat().statusCode(SC_OK);
            log.info("Accounts ingested for Bill Pay user [{}]", subscriberId);
        }
    }

    private Subscriber createSubscriber(String subscriberId, List<Account> accounts) {
        return new Subscriber()
            .withUser(subscriberId)
            .withSubscriberId(subscriberId)
            .withFirstName("John")
            .withLastName("Doe")
            .withTaxIdentifier("tax00001")
            .withAddress(
                new Address()
                    .withAddress1("560 Carillon Parkway")
                    .withAddress2("Carillon")
                    .withCity("Saint Petersburg")
                    .withState("FL")
                    .withPostalCode("33717")
            )
            .withAccounts(accounts);
    }
}
