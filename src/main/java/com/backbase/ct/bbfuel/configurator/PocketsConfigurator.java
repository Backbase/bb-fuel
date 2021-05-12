package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.bbfuel.client.pfm.PocketsRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator;
import com.backbase.ct.bbfuel.input.PocketsReader;
import com.backbase.ct.bbfuel.service.AccessGroupService;
import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
import com.backbase.dbs.pocket.tailor.client.v2.model.Pocket;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import java.util.ArrayList;
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
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final AccessGroupService accessGroupService;

    /**
     * Ingest pocket parent arrangement.
     *
     * @param legalEntity legalEntity
     */
    public ArrangementsPostResponseBody ingestPocketParentArrangement(LegalEntityBase legalEntity) {
        log.debug("Going to ingest a parent pocket arrangement for external legal entity ID: [{}]",
            legalEntity);
        ArrangementsPostRequestBody parentPocketArrangement = ProductSummaryDataGenerator
            .generateParentPocketArrangement(legalEntity.getExternalId());
        ArrangementsPostResponseBody response = arrangementsIntegrationRestClient
            .ingestArrangement(parentPocketArrangement);

        log.info("Parent pocket arrangement ingested for external legal entity ID [{}]: ID {}, name: {}",
            legalEntity,
            response.getId(), parentPocketArrangement.getName());

        //TODO data group ingestion for above created arrangement must be done.
        // so first retrieve service agreement with externalLegalEntityId
        log.debug("getting legalEntityPresentationRestClient.getMasterServiceAgreementOfLegalEntity for [{}]",
            legalEntity.getId());
        String internalServiceAgreementId = this.legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .getId();
        log.debug("getting serviceAgreementsPresentationRestClient.retrieveServiceAgreement for [{}]",
            internalServiceAgreementId);
        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();
        log.debug("Going to ingest a ingest datagroup for service agreement: [{}]", externalServiceAgreementId);
        //TODO ingest datagroup, hardcoded datagroup name for now from data/retail/legal-entities-with-user.json
        List<String> internalArrangementIds = new ArrayList<>();
        internalArrangementIds.add(response.getId());
        String dataGroupId = accessGroupService
            .ingestDataGroup(externalServiceAgreementId, "Retail Accounts U.S", "ARRANGEMENTS", internalArrangementIds);
        log.debug("created data group with id [{}]", dataGroupId);
        //TODO after this code has run, there is no entry in the table data_group_item for the arrangement from response.getId(), why?
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
