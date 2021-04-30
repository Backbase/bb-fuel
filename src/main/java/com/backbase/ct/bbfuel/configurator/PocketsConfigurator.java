package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.pfm.PocketsRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.PocketsDataGenerator;
import com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator;
import com.backbase.ct.bbfuel.input.PocketsReader;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PocketsConfigurator {

    private static final GlobalProperties GLOBAL_PROPERTIES = GlobalProperties.getInstance();

    private final PocketsReader pocketsReader = new PocketsReader();

    private final ArrangementsIntegrationRestClient arrangementsIntegrationRestClient;
    private final PocketsRestClient pocketsRestClient;
    private final LoginRestClient loginRestClient;

    /**
     * Ingest pocket parent arrangement.
     *
     * @param externalLegalEntityId externalLegalEntityId
     * @param externalArrangementId externalArrangementId
     */
    public void ingestPocketParentArrangement(String externalLegalEntityId, String externalArrangementId) {
        log.debug("Going to ingest a parent pocket arrangement.");
        ArrangementsPostRequestBody parentPocketArrangement = ProductSummaryDataGenerator
            .generateParentPocketArrangement(externalLegalEntityId, externalArrangementId);
        ArrangementsPostResponseBody response = arrangementsIntegrationRestClient
            .ingestArrangement(parentPocketArrangement);

        log.info("Parent pocket arrangement ingested for [{}, {}]: ID {}, name: {}", externalLegalEntityId,
            externalArrangementId, response.getId(), parentPocketArrangement.getName());
    }

    /**
     * Ingest Pockets.
     *
     * @param externalUserId externalUserId
     * @param isRetail       isRetail
     */
    public void ingestPockets(String externalUserId, boolean isRetail) {
        log.debug("Going to ingest pockets.");

        loginRestClient.login(externalUserId, externalUserId);
        log.debug("logged in as {}", externalUserId);

        List<PocketPostRequest> pockets = new ArrayList<>();

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(GLOBAL_PROPERTIES.getInt(CommonConstants.PROPERTY_POCKETS_MIN),
                GLOBAL_PROPERTIES.getInt(CommonConstants.PROPERTY_POCKETS_MAX));

        if (isRetail) {
            log.debug("Generating pockets data from json file.");
            IntStream.range(0, randomAmount).forEach(randomNumber -> pockets.add(pocketsReader.loadSingle()));
        } else {
            log.debug("Generating pockets data with faker.");
            IntStream.range(0, randomAmount).forEach(randomNumber -> pockets.add(
                PocketsDataGenerator.generatePocketPostRequest()));
        }

        for (PocketPostRequest pocketPostRequest : pockets) {
            Response response = pocketsRestClient.ingestPocket(pocketPostRequest);

            if (response.statusCode() == SC_BAD_REQUEST) {
                log.info("Bad request for ingesting pocket with request [{}]", pocketPostRequest);
            } else {
                response.then().assertThat().statusCode(SC_CREATED);
                log.info("Pocket ingested with request [{}]", pocketPostRequest);
            }
        }

    }
}
