package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.accountstatement.AccountStatementsIntegrationMockServiceApiClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.backbase.ct.bbfuel.data.AccountStatementDataGenerator.generateAccountStatementsRequests;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_MIN;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static org.apache.http.HttpStatus.SC_CREATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountStatementsConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final AccountStatementsIntegrationMockServiceApiClient AccountStatementsIntegrationMockServiceApiClient;

    public void ingestAccountStatements(String externalUserId) {
        List<ArrangementsByBusinessFunctionGetResponseBody> arrangements = new ArrayList<>();

        int randomAmount = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ACCOUNTSTATEMENTS_MIN),
                globalProperties.getInt(PROPERTY_ACCOUNTSTATEMENTS_MAX));

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        arrangements.addAll(productSummaryPresentationRestClient.getSepaCtArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getUsDomesticWireArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getAchDebitArrangements());

                for (ArrangementsByBusinessFunctionGetResponseBody arrangement : arrangements) {
                    String internalArrangementId = arrangement.getId();
                    String accountName = arrangement.getName();
                    String accountIBAN = arrangement.getIBAN();

                    AccountStatementsIntegrationMockServiceApiClient.createAccountStatements(generateAccountStatementsRequests(randomAmount, externalUserId, internalArrangementId, accountName, accountIBAN)).then().statusCode(SC_CREATED);

                    log.info("Account Statement ingested with  id [{}] for account Name [{}] and account Number [{}] for user [{}]",
                            internalArrangementId, accountName, accountIBAN, externalUserId);

                }
    }
}
