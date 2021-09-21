package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.billpay.BillPayMockBankRestClient;
import com.backbase.ct.bbfuel.client.billpay.BillPayPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.productsummary.AccountsIntegrationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.dbs.billpay.client.v2.model.Subscriber;
import com.backbase.dbs.billpay.client.v2.model.SubscriberAccount;
import com.backbase.dbs.billpay.client.v2.model.SubscriberAddress;
import com.backbase.dbs.billpay.client.v2.model.UserByIdPutRequestBody;
import com.backbase.dbs.arrangement.integration.outbound.link.v2.model.ArrangementItem;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
    private final AccountsIntegrationRestClient accountsIntegrationRestClient;
    private final UserPresentationRestClient userPresentationRestClient;

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

    private void ingestBillPayAccounts(String subscriberId, List<SubscriberAccount> accounts) {
        loginRestClient.login(subscriberId, subscriberId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        UserByIdPutRequestBody request = new UserByIdPutRequestBody()
            .subscriber(createSubscriber(subscriberId, accounts));
        Response response = billPayMockBankRestClient.ingestAccounts(request);
        if (isBadRequestException(response, "The request is invalid")) {
            log.info("Bad request for Bill Pay user [{}]", subscriberId);
        } else {
            response.then().assertThat().statusCode(SC_OK);
            log.info("Accounts ingested for Bill Pay user [{}]", subscriberId);
        }
    }

    public void ingestBillPayUserAndAccounts(LegalEntityWithUsers le, Boolean ingestAccounts) {
        List<SubscriberAccount> accounts = new ArrayList<>();
        if (ingestAccounts) {
            String externalUserId = le.getUserExternalIds().get(0);
            loginRestClient.login(externalUserId, externalUserId);
            String externalLegalEntityId = this.userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(externalUserId)
                .getExternalId();
            List<ArrangementItem> arrangementItems = accountsIntegrationRestClient
                .getArrangements(externalLegalEntityId);

            arrangementItems.forEach((arrangement) -> {
                accounts.add(mapBillPayAccount(arrangement));
            });
        }
        le.getUserExternalIds().forEach((user) -> {
            ingestBillPayUser(user);
            if (ingestAccounts) {
                ingestBillPayAccounts(user, accounts);
            }
        });
    }

    private Subscriber createSubscriber(String subscriberId, List<SubscriberAccount> accounts) {
        return new Subscriber()
            .user(subscriberId)
            .subscriberId(subscriberId)
            .firstName("John")
            .lastName("Doe")
            .taxIdentifier("tax00001")
            .address(
                new com.backbase.dbs.billpay.client.v2.model.SubscriberAddress()
                    .address1("560 Carillon Parkway")
                    .address2("Carillon")
                    .city("Saint Petersburg")
                    .state("FL")
                    .postalCode("33717")
            )
            .accounts(accounts);
    }

    public SubscriberAccount mapBillPayAccount(ArrangementItem arrangement) {
        return new SubscriberAccount()
            .accountNumber(arrangement.getBBAN())
            .routingNumber(arrangement.getBankBranchCode())
            .accountType(CommonHelpers.getRandomFromList(Arrays.asList(SubscriberAccount.AccountTypeEnum.values())));
    }
}
