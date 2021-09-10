package com.backbase.ct.bbfuel.configurator;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.positivepay.PositivePayRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.data.PositivePayDataGenerator;
import com.backbase.ct.bbfuel.dto.ArrangementId;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.Subscription;
import com.backbase.dbs.positivepay.client.api.v1.model.PositivePayPost;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_POSITIVEPAY_MIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_POSITIVEPAY_MAX;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.http.HttpStatus.SC_OK;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositivePayConfigurator {

    private static final String SUBSCRIPTION_POSITIVE_PAY_WITHOUT_PAYEE_MATCH = "checks-positive-pay-without-payee-match";
    private static final String SUBSCRIPTION_POSITIVE_PAY_WITH_PAYEE_MATCH = "checks-positive-pay-with-payee-match";
    private static final String SUBSCRIPTION_ACH_POSITIVE_PAY = "ach-positive-pay";
    private static final List<String> CHECK_SUBSCRIPTIONS = newArrayList(SUBSCRIPTION_POSITIVE_PAY_WITHOUT_PAYEE_MATCH,
        SUBSCRIPTION_POSITIVE_PAY_WITH_PAYEE_MATCH);

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private PositivePayDataGenerator positivePayDataGenerator = new PositivePayDataGenerator();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final PositivePayRestClient PositivePayRestClient;
    private final ArrangementsIntegrationRestClient arrangementsIntegrationRestClient;

    public void ingestPositivePayChecks(String externalUserId) {
        List<ArrangementsByBusinessFunctionGetResponseBody> arrangements = new ArrayList<>();

        int randomAmount = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_POSITIVEPAY_MIN),
                globalProperties.getInt(PROPERTY_POSITIVEPAY_MAX));

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        arrangements.addAll(productSummaryPresentationRestClient.getSepaCtArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getUsDomesticWireArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getAchDebitArrangements());

        log.info("Positive Pay check submitting for user [{}]", externalUserId);

        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            String internalArrangementId = getRandomFromList(arrangements).getId();

            PositivePayPost positivePayPostRequestBody = positivePayDataGenerator.generatePositivePayPostRequestBody(internalArrangementId);

            PositivePayRestClient.submitPositivePayChecks(positivePayPostRequestBody).then().statusCode(SC_OK);

            log.info("Positive Pay check submitted for arrangement id [{}] from user [{}]",
                    internalArrangementId, externalUserId);

        });
    }

    public void ingestPositivePaySubscriptions(ArrangementId arrangementId) {
        // Assigning a check subscription
        String randomSubscription = getRandomFromList(CHECK_SUBSCRIPTIONS);
        arrangementsIntegrationRestClient.postSubscriptions(arrangementId.getExternalArrangementId(),
            new Subscription().identifier(randomSubscription));
        log.info("Positive Pay check subscription : [{}] submitted for arrangement id [{}]",
            randomSubscription, arrangementId.getInternalArrangementId());

        // Assigning an ACH subscription
        arrangementsIntegrationRestClient.postSubscriptions(arrangementId.getExternalArrangementId(),
            new Subscription().identifier(SUBSCRIPTION_ACH_POSITIVE_PAY));
        log.info("Positive Pay ACH subscription submitted for arrangement id [{}]",
            arrangementId.getInternalArrangementId());
    }
}
