package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.pfm.PocketsMockArrangementRestClient;
import com.backbase.ct.bbfuel.client.pfm.PocketsRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.PocketsDataGenerator;
import com.backbase.ct.bbfuel.dto.ArrangementId;
import com.backbase.ct.bbfuel.input.PocketsReader;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.arrangement.integration.outbound.origination.v1.model.CreateArrangementRequest;
import com.backbase.dbs.pocket.tailor.client.v1.model.PocketPostRequest;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Collections;
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
    public static final String PRODUCT_ID = "8";
    public static final String PRODUCT_KIND_ID = "kind8";

    private final PocketsReader pocketsReader = new PocketsReader();

    private final PocketsRestClient pocketsRestClient;
    private final PocketsMockArrangementRestClient pocketsMockArrangementRestClient;

    /**
     * Ingest pocket parent arrangement.
     *
     * @param externalLegalEntityId      externalLegalEntityId
     * @param externalServiceAgreementId externalServiceAgreementId
     */
    public void ingestPocketParentArrangement(String externalLegalEntityId, String externalServiceAgreementId,
        String externalUserId) {
        log.debug("Going to ingest a pocket parent arrangement.");
        CreateArrangementRequest createArrangementRequest = new CreateArrangementRequest();
        createArrangementRequest
            .externalProductId(PRODUCT_ID)
            .externalProductKindId(PRODUCT_KIND_ID)
            .externalLegalEntityId(externalLegalEntityId)
            .serviceAgreementId(externalServiceAgreementId)
            .externalUserId(externalUserId)
            .name("pocket")
            .additions(Collections.singletonMap("name", "value"));

        Response response = pocketsMockArrangementRestClient.ingestPocketParentArrangement(createArrangementRequest);
        if (isBadRequestException(response, "The request is invalid")) {
            log.info("Bad request for ingesting parent pocket arrangement for [{}, {}]", externalLegalEntityId,
                externalServiceAgreementId);
        } else {
            response.then().assertThat().statusCode(SC_CREATED);
            log.info("Pocket parent arrangement ingested for [{}, {}]", externalLegalEntityId,
                externalServiceAgreementId);
        }
    }

    /**
     * Ingest Pockets.
     *
     * @param arrangementId arrangementId
     * @param isRetail      isRetail
     */
    public void ingestPockets(ArrangementId arrangementId, boolean isRetail) {
        log.debug("Going to ingest pockets for [{}, {}]", arrangementId.getExternalArrangementId(),
            arrangementId.getInternalArrangementId());
        List<PocketPostRequest> pockets = Collections
            .synchronizedList(new ArrayList<>());

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(GLOBAL_PROPERTIES.getInt(CommonConstants.PROPERTY_POCKETS_MIN),
                GLOBAL_PROPERTIES.getInt(CommonConstants.PROPERTY_POCKETS_MAX));

        if (isRetail) {
            log.debug("Generating pockets data from json file.");
            IntStream.range(0, randomAmount).forEach(randomNumber -> pockets.add(
                pocketsReader.loadSingle()));

        } else {
            log.debug("Generating pockets data with faker.");
            IntStream.range(0, randomAmount).forEach(randomNumber -> pockets.add(
                PocketsDataGenerator.generatePocketPostRequest()));
        }

        for (PocketPostRequest pocketPostRequest : pockets) {
            Response response = pocketsRestClient.ingestPocket(pocketPostRequest);

            if (isBadRequestException(response, "The request is invalid")) {
                log.info("Bad request for ingesting pockets for [{}]", arrangementId);
            } else {
                response.then().assertThat().statusCode(SC_OK);
                log.info("Pockets ingested for [{}]", arrangementId);
            }
        }

    }
}
