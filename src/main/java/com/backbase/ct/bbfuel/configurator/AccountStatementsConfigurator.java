package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.AccountStatementDataGenerator.generateAccountStatementsRequests;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_MIN;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.accountstatement.AccountStatementsClient;
import com.backbase.ct.bbfuel.client.accountstatement.AccountStatementsPreferencesClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.dto.accountStatement.EStatementPreferencesRequest;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountStatementsConfigurator {

    private static final GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final AccountStatementsClient AccountStatementsClient;
    private final AccountStatementsPreferencesClient accountStatementsPreferencesClient;

    public void ingestAccountStatements(String externalUserId) {
        int randomAmount = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ACCOUNTSTATEMENTS_MIN),
            globalProperties.getInt(PROPERTY_ACCOUNTSTATEMENTS_MAX));

        Consumer<ArrangementsByBusinessFunctionGetResponseBody> consumer = arrangement -> {
            String internalArrangementId = arrangement.getId();
            String accountName = arrangement.getName();
            String accountIBAN = arrangement.getIBAN();

            AccountStatementsClient.createAccountStatements(
                generateAccountStatementsRequests(randomAmount, externalUserId, internalArrangementId, accountName,
                    accountIBAN)).then().statusCode(SC_CREATED);

            log.info(
                "Account Statement ingested with  id [{}] for account Name [{}] and account Number [{}] for user [{}]",
                internalArrangementId, accountName, accountIBAN, externalUserId);
        };

        ingest(externalUserId)
            .forEach(consumer);
    }

    public void ingestAccountStatementPreferences(String externalUserId) {
        final Random booleanGenerator = new Random(System.currentTimeMillis());

        Function<ArrangementsByBusinessFunctionGetResponseBody, EStatementPreferencesRequest> mapper = arrangement -> new EStatementPreferencesRequest(
            arrangement.getId(), externalUserId,
            booleanGenerator.nextBoolean(), booleanGenerator.nextBoolean());

        accountStatementsPreferencesClient.createAccountStatementsPreferences(ingest(externalUserId)
            .map(mapper)
            .collect(toList()));
    }

    private Stream<ArrangementsByBusinessFunctionGetResponseBody> ingest(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        return of(productSummaryPresentationRestClient.getProductSummaryArrangements().stream(),
            productSummaryPresentationRestClient.getSepaCtArrangements().stream(),
            productSummaryPresentationRestClient.getUsDomesticWireArrangements().stream(),
            productSummaryPresentationRestClient.getAchDebitArrangements().stream())
            .flatMap(identity());
    }
}
