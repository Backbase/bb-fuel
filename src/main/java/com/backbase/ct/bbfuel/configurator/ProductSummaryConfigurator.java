package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateBalanceHistoryPostRequestBodies;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateCurrentAccountArrangementsPostRequestBodies;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateNonCurrentAccountArrangementsPostRequestBodies;
import static java.util.Collections.synchronizedList;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator;
import com.backbase.ct.bbfuel.dto.ArrangementId;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.balancehistory.BalanceHistoryPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
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
    private final ArrangementsIntegrationRestClient arrangementsIntegrationRestClient;

    public void ingestProducts() {
        List<ProductsPostRequestBody> products = ProductSummaryDataGenerator.getProductsFromFile();
        products.stream().parallel()
            .forEach(arrangementsIntegrationRestClient::ingestProductAndLogResponse);
    }

    public List<ArrangementId> ingestArrangements(String externalLegalEntityId, ProductGroupSeed productGroupSeed) {
        List<ArrangementsPostRequestBody> arrangements = synchronizedList(new ArrayList<>());
        List<ArrangementId> arrangementIds = synchronizedList(new ArrayList<>());

        int numberOfArrangements = productGroupSeed.getNumberOfArrangements().getNumberInRange();
        int tenPercentOfTotal = (int) Math.round(numberOfArrangements * 0.1);
        int numberOfNonCurrentAccounts = tenPercentOfTotal > 0 ? tenPercentOfTotal : 0;

        if (productGroupSeed.getProductIds().contains(String.valueOf(1))) {
            productGroupSeed.getNumberOfArrangements().setNumberInRange(numberOfArrangements - numberOfNonCurrentAccounts);

            arrangements.addAll(generateCurrentAccountArrangementsPostRequestBodies(
                externalLegalEntityId, productGroupSeed));

            productGroupSeed.getProductIds().remove(String.valueOf(1));
        }

        if (numberOfNonCurrentAccounts > 0 && !productGroupSeed.getProductIds().isEmpty()) {
            productGroupSeed.getNumberOfArrangements().setNumberInRange(
                productGroupSeed.getProductIds().contains(String.valueOf(1)) ? numberOfNonCurrentAccounts : numberOfArrangements);

            arrangements.addAll(generateNonCurrentAccountArrangementsPostRequestBodies(
                externalLegalEntityId, productGroupSeed));
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
