package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.pfm.PocketsRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator;
import com.backbase.ct.bbfuel.input.PocketsReader;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.dbs.pocket.tailor.client.v2.model.Pocket;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PocketsConfigurator {

    private final PocketsReader pocketsReader = new PocketsReader();
    private final ArrangementsIntegrationRestClient arrangementsIntegrationRestClient;
    private final PocketsRestClient pocketsRestClient;

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
     * Ingests Pockets for the given user.
     *
     * @param externalUserId External user ID to ingest pockets for.
     */
    public void ingestPockets(String externalUserId) {
        log.debug("Going to ingest pockets for user [{}]", externalUserId);

        log.debug("Generating pockets data from json file");
        List<PocketPostRequest> pockets = pocketsReader.load();

        for (PocketPostRequest pocketPostRequest : pockets) {
            Pocket pocket = pocketsRestClient.ingestPocket(pocketPostRequest);
            log.info("Pocket with ID [{}] and name [{}] created for user [{}]", pocket.getId(), pocket.getName(),
                externalUserId);
        }
    }
}
