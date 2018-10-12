package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MIN;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateBalanceHistoryPostRequestBodies;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateCurrentAccountArrangementsPostRequestBodies;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateNonCurrentAccountArrangementsPostRequestBodies;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static java.util.Collections.synchronizedList;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.IngestException;
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
        List<ProductsPostRequestBody> products = ProductSummaryDataGenerator.generateProductsPostRequestBodies();
        products.stream().parallel()
            .forEach(arrangementsIntegrationRestClient::ingestProductAndLogResponse);
    }

    public synchronized List<ArrangementId> ingestArrangements(String externalLegalEntityId, List<Currency> currencies,
        List<String> currentAccountNames, List<String> productIds) {
        List<ArrangementsPostRequestBody> arrangements = new ArrayList<>();
        List<ArrangementId> arrangementIds = synchronizedList(new ArrayList<>());
        int totalNumberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MAX));

        int tenPercentOfTotal = (int) Math.round(totalNumberOfArrangements * 0.1);
        int numberOfNonCurrentAccounts = tenPercentOfTotal > 0 ? tenPercentOfTotal : 0;
        int numberOfCurrentAccounts = totalNumberOfArrangements - numberOfNonCurrentAccounts;

        if (productIds.isEmpty()) {
            throw new IngestException("No product ids found in input file");
        }

        if (productIds.contains(String.valueOf(1))) {
            arrangements.addAll(generateCurrentAccountArrangementsPostRequestBodies(
                    externalLegalEntityId, currencies, currentAccountNames, numberOfCurrentAccounts));
        }

        productIds.remove(String.valueOf(1));

        if (numberOfNonCurrentAccounts > 0 && !productIds.isEmpty()) {
            arrangements.addAll(generateNonCurrentAccountArrangementsPostRequestBodies(
                    externalLegalEntityId, currencies, productIds, numberOfNonCurrentAccounts));
        }

        arrangements.parallelStream().forEach(arrangement -> {
            ArrangementsPostResponseBody arrangementsPostResponseBody = arrangementsIntegrationRestClient
                .ingestArrangement(arrangement);
            LOGGER.info("Arrangement [{}] ingested for product [{}] under legal entity [{}]",
                arrangement.getName(), arrangement.getProductId(), externalLegalEntityId);
            arrangementIds.add(new ArrangementId(arrangementsPostResponseBody.getId(), arrangement.getId()));
        });

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
