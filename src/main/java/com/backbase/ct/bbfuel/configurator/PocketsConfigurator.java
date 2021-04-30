package com.backbase.ct.bbfuel.configurator;

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
import com.backbase.dbs.pocket.tailor.client.v1.model.Pocket;
import com.backbase.dbs.pocket.tailor.client.v1.model.PocketPostRequest;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
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
     */
    public ArrangementsPostResponseBody ingestPocketParentArrangement(String externalLegalEntityId) {
        log.debug("Going to ingest a parent pocket arrangement for external legal entity ID: [{}]",
            externalLegalEntityId);
        ArrangementsPostRequestBody parentPocketArrangement = ProductSummaryDataGenerator
            .generateParentPocketArrangement(externalLegalEntityId);
        ArrangementsPostResponseBody response = arrangementsIntegrationRestClient
            .ingestArrangement(parentPocketArrangement);

        log.info("Parent pocket arrangement ingested for external legal entity ID [{}]: ID {}, name: {}",
            externalLegalEntityId,
            response.getId(), parentPocketArrangement.getName());

        return response;
    }

    /**
     * Ingest Pockets.
     *
     * @param externalUserId externalUserId
     * @param isRetail       isRetail
     */
    public void ingestPockets(String externalUserId, boolean isRetail) {
        log.debug("Going to ingest pockets for user [{}]", externalUserId);

        loginRestClient.login(externalUserId, externalUserId);

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
            Pocket pocket = pocketsRestClient.ingestPocket(pocketPostRequest);
            log.info("Pocket with ID [{}] and name [{}] created for user [{}]", pocket.getId(), pocket.getName(),
                externalUserId);
        }

    }
}
