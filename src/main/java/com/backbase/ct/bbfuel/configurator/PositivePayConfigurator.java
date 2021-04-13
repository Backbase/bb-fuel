package com.backbase.ct.bbfuel.configurator;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.positivepay.PositivePayRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.data.PositivePayDataGenerator;
import com.backbase.ct.bbfuel.util.GlobalProperties;
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
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositivePayConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private PositivePayDataGenerator positivePayDataGenerator = new PositivePayDataGenerator();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final PositivePayRestClient PositivePayRestClient;

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
}
