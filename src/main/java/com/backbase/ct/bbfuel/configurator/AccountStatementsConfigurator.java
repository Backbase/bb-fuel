package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.AccountStatementDataGenerator.generateAccountStatementsRequests;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_MIN;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static java.util.stream.Collectors.toList;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.accountstatement.AccountStatementsClient;
import com.backbase.ct.bbfuel.client.accountstatement.AccountStatementsPreferencesClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.client.tokenconverter.TokenConverterServiceApiClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserProfileRestClient;
import com.backbase.ct.bbfuel.dto.accountStatement.EStatementPreferencesRequest;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.arrangement.client.api.v2.model.ProductSummaryItem;
import io.restassured.response.Response;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountStatementsConfigurator {

    private static final int RETRY_LIMIT = 2;
    private static final GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static int retryCounter;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final AccountStatementsClient accountStatementsClient;
    private final AccountStatementsPreferencesClient accountStatementsPreferencesClient;
    private final UserProfileRestClient userProfileRestClient;
    private final UserPresentationRestClient userPresentationRestClient;
    private final TokenConverterServiceApiClient tokenConverter;

    public void ingestAccountStatements(String externalUserId) {
        int randomAmount = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ACCOUNTSTATEMENTS_MIN),
            globalProperties.getInt(PROPERTY_ACCOUNTSTATEMENTS_MAX));

        Consumer<ProductSummaryItem> consumer = arrangement -> {
            String internalArrangementId = arrangement.getId();
            String accountName = arrangement.getName();
            String accountIBAN = arrangement.getIBAN();

            accountStatementsClient.createAccountStatements(tokenConverter,
                generateAccountStatementsRequests(randomAmount, externalUserId, internalArrangementId, accountName,
                    accountIBAN)).then().statusCode(SC_CREATED);

            log.info(
                "Account Statement ingested with  id [{}] for account Name [{}] and account Number [{}] for user [{}]",
                internalArrangementId, accountName, accountIBAN, externalUserId);
        };

        fetchUserArrangements(externalUserId)
            .forEach(consumer);
    }

    public void ingestAccountStatementPreferences(String externalUserId) {
        final Random booleanGenerator = new Random(System.currentTimeMillis());

        Function<ProductSummaryItem, EStatementPreferencesRequest> mapper =
            arrangement -> new EStatementPreferencesRequest(
                arrangement.getId(), externalUserId,
                booleanGenerator.nextBoolean(), booleanGenerator.nextBoolean());

        List<EStatementPreferencesRequest> eStatementPreferencesRequests = fetchUserArrangements(externalUserId)
            .stream()
            .map(mapper)
            .collect(toList());

        accountStatementsPreferencesClient.createAccountStatementsPreferences(tokenConverter, eStatementPreferencesRequests);
    }

    public void ingestUserProfile(String externalUserId) {
        loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        String userId = userPresentationRestClient.getUserByExternalId(externalUserId).getId();
        if (userId.isEmpty()) {
            log.warn("User profile for externalId [{}] WAS NOT CREATED, because such user was not found", externalUserId);
        }
        Response userProfileCreationResponse = userProfileRestClient.createUserProfile(userId, externalUserId);
        if (userProfileCreationResponse.getStatusCode() == SC_BAD_REQUEST) {
            log.error("User profile for externalId [{}] and userId [{}] WAS NOT CREATED", externalUserId, userId);
            log.error("Response: " + userProfileCreationResponse.body().asString());
        }
        if (userProfileCreationResponse.getStatusCode() == SC_INTERNAL_SERVER_ERROR) {
            log.warn("User profile for externalId [{}] and userId [{}] was not created due to server error.",
                externalUserId, userId);
            retryCounter++;
            if (retryCounter < RETRY_LIMIT) {
                // This retry is because of 500 returned from user-profile-manager when service is warming up
                log.info("Retrying creation in 10 seconds.");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ingestUserProfile(externalUserId);
            }
            log.warn(userProfileCreationResponse.getStatusLine());
        }
        if (userProfileCreationResponse.getStatusCode() == SC_CREATED) {
            log.info("Ingested user profile for externalId [{}] and userId [{}]", externalUserId, userId);
        } else {
            log.error("User profile for externalId [{}] and userId [{}] was not ingested.", externalUserId, userId);
        }
    }

    private List<ProductSummaryItem> fetchUserArrangements(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        return productSummaryPresentationRestClient.getProductSummaryArrangements();
    }
}
