package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MIN;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateBalanceHistoryPostRequestBodies;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateCurrentAccountArrangementsPostRequestBodies;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateNonCurrentAccountArrangementsPostRequestBodies;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.ArrangementType;
import com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator;
import com.backbase.ct.bbfuel.dto.ArrangementId;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSummaryConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSummaryConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final ArrangementsIntegrationRestClient arrangementsIntegrationRestClient;

    public void ingestProducts() throws IOException {
        ProductsPostRequestBody[] products = ProductSummaryDataGenerator.generateProductsPostRequestBodies();
        Arrays.stream(products).parallel()
            .forEach(arrangementsIntegrationRestClient::ingestProductAndLogResponse);
    }

    public List<ArrangementId> ingestArrangements(String externalLegalEntityId, List<Currency> currencies,
        List<String> currentAccountNames, List<String> productIds, boolean isRetail) {
        List<ArrangementsPostRequestBody> arrangements = new ArrayList<>();
        List<ArrangementId> arrangementIds = new ArrayList<>();
        int totalNumberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MAX));

        int tenPercentOfTotal = (int) Math.round(totalNumberOfArrangements * 0.1);
        int numberOfNonCurrentAccounts = tenPercentOfTotal > 0 ? tenPercentOfTotal : 0;
        int numberOfCurrentAccounts = totalNumberOfArrangements - numberOfNonCurrentAccounts;

        arrangements.addAll(generateCurrentAccountArrangementsPostRequestBodies(
            externalLegalEntityId, currencies, currentAccountNames, numberOfCurrentAccounts));

        if (numberOfNonCurrentAccounts > 0) {
            arrangements.addAll(generateNonCurrentAccountArrangementsPostRequestBodies(
                    externalLegalEntityId, currencies, productIds, numberOfNonCurrentAccounts);
        }

        for (ArrangementsPostRequestBody arrangement : arrangements) {
            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient
                .ingestArrangement(arrangement);

            LOGGER.info("Arrangement [{}] ingested for product [{}] under legal entity [{}]",
                arrangement.getName(), arrangement.getProductId(), externalLegalEntityId);

            arrangementIds.add(new ArrangementId(arrangementsPostResponseBody.getId(), arrangement.getId()));
        }

        return arrangementIds;
    }

    public List<ArrangementId> ingestArrangements(String externalLegalEntityId, ArrangementType arrangementType,
        Currency currency) {
        List<ArrangementId> arrangementIds = new ArrayList<>();

        List<ArrangementsPostRequestBody> arrangements = ProductSummaryDataGenerator
            .generateArrangementsPostRequestBodies(externalLegalEntityId, arrangementType, currency);

        for (ArrangementsPostRequestBody arrangement : arrangements) {
            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient
                .ingestArrangement(arrangement);

            LOGGER.info("Arrangement [{}] ingested for product [{}] under legal entity [{}]",
                arrangement.getName(), arrangement.getProductId(), externalLegalEntityId);

            arrangementIds.add(new ArrangementId(arrangementsPostResponseBody.getId(), arrangement.getId()));
        }

        return arrangementIds;
    }

    public void ingestBalanceHistory(String externalArrangementId) {
        List<BalanceHistoryPostRequestBody> balanceHistoryPostRequestBodies = generateBalanceHistoryPostRequestBodies(
            externalArrangementId);

        balanceHistoryPostRequestBodies.parallelStream()
            .forEach(balanceHistoryPostRequestBody -> {
                arrangementsIntegrationRestClient.ingestBalance(balanceHistoryPostRequestBody)
                    .then()
                    .statusCode(SC_CREATED);

                LOGGER.info("Balance history item ingested for arrangement [{}] with updated date [{}]",
                    externalArrangementId, balanceHistoryPostRequestBody.getUpdatedDate());
            });
    }
}
