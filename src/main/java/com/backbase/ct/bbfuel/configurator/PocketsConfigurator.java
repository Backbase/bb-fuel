package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.bbfuel.client.pfm.PocketsRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ArrangementsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator;
import com.backbase.ct.bbfuel.input.PocketsReader;
import com.backbase.ct.bbfuel.service.AccessGroupService;
import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.ArrangementAddedResponse;
import com.backbase.dbs.arrangement.integration.inbound.api.v2.model.PostArrangement;
import com.backbase.dbs.pocket.tailor.client.v2.model.Pocket;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PocketsConfigurator {

    public static final String EXTERNAL_ARRANGEMENT_ORIGINATION = "external-arrangement-origination-";
    public static final String EXTERNAL_ARRANGEMENT_ORIGINATION_1 = EXTERNAL_ARRANGEMENT_ORIGINATION + "1";
    private final PocketsReader pocketsReader = new PocketsReader();
    private final ArrangementsIntegrationRestClient arrangementsIntegrationRestClient;
    private final PocketsRestClient pocketsRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final AccessGroupService accessGroupService;

    /**
     * Ingest pocket arrangement for 1-to-many mode.
     *
     * @param legalEntity legal entity
     * @return pocket parent arrangement id as String
     */
    public String ingestPocketArrangementForModeOnetoManyAndSetEntitlements(LegalEntityBase legalEntity) {
        // -> creating parent pocket arrangement for legal entity
        log.debug("Going to ingest a parent pocket arrangement for external legal entity ID: [{}]", legalEntity);

        String parentPocketArrangementId = null;
        ArrangementAddedResponse arrangementsPostResponseBody = ingestParentPocketArrangement(legalEntity);

        if (arrangementsPostResponseBody != null) {
            parentPocketArrangementId = arrangementsPostResponseBody.getId();
            log.info("Parent pocket arrangement ingested for external legal entity ID [{}]: ID {}",
                legalEntity, parentPocketArrangementId);

            // -> Now setting entitlements by dataGroup (functionGroup is already managed by setting
            //      permissions in retail/job-profiles.json: jobProfileName: 'Retail User' )
            // -> Updating dataGroup makes the method accessControlClient.verifyCreateAccessToArrangement(arrangementId)
            //      in PocketTailorServiceImpl succeed, by returning all relevant arrangements with
            //      usersApi.getArrangementPrivileges
            // -> Updating functionGroup makes @PreAuthorize("checkPermission... in PocketTailorServiceImpl work, by
            //      AccessControlValidatorImpl.checkPermissions succeed
            updateDataGroupForPockets(parentPocketArrangementId, legalEntity);
        }

        return parentPocketArrangementId;
    }

    /**
     * Ingest pocket arrangement for 1-to-1 mode.
     *
     * @param legalEntity legal entity
     */
    public void ingestPocketArrangementForModeOnetoOneAndSetEntitlements(
        LegalEntityBase legalEntity, int counter) {
        // -> creating pocket arrangement for legal entity
        log.debug("Going to ingest a pocket arrangement for external legal entity ID: [{}]", legalEntity);

        String pocketArrangementId;
        ArrangementAddedResponse arrangementAddedResponse = ingestPocketArrangement(legalEntity, counter);

        if (arrangementAddedResponse != null) {
            pocketArrangementId = arrangementAddedResponse.getId();
            log.info("Pocket arrangement ingested for external legal entity ID [{}]: ID {}",
                legalEntity, pocketArrangementId);

            // -> Now setting entitlements by dataGroup (functionGroup is already managed by setting
            //      permissions in retail/job-profiles.json: jobProfileName: 'Retail User' )
            // -> Updating dataGroup makes the method accessControlClient.verifyCreateAccessToArrangement(arrangementId)
            //      in PocketTailorServiceImpl succeed, by returning all relevant arrangements with
            //      usersApi.getArrangementPrivileges
            // -> Updating functionGroup makes @PreAuthorize("checkPermission... in PocketTailorServiceImpl work, by
            //      AccessControlValidatorImpl.checkPermissions succeed
            updateDataGroupForPockets(pocketArrangementId, legalEntity);
        }
    }

    /**
     * Ingests Pockets for the given user.
     *
     * @param externalUserId External user ID to ingest pockets for.
     * @return list of ingested pockets
     */
    public List<Pocket> ingestPockets(String externalUserId) {
        log.debug("Going to ingest pockets for user [{}]", externalUserId);
        Builder<Pocket> pocketBuilder = ImmutableList.builder();
        List<PocketPostRequest> pockets = pocketsReader.load();

        for (PocketPostRequest pocketPostRequest : pockets) {
            Pocket pocket = pocketsRestClient.ingestPocket(pocketPostRequest);
            log.info("Pocket with ID [{}] and name [{}] created for user [{}]", pocket.getId(), pocket.getName(),
                externalUserId);
            pocketBuilder.add(pocket);
        }
        return pocketBuilder.build();
    }

    /**
     * Ingest Pocket for the given user.
     *
     * @param externalUserId External user ID to ingest pockets for.
     * @param counter counter to select specific pocket from reader load
     */
    public void ingestPocket(String externalUserId, int counter) {
        log.debug("Going to ingest pockets for user [{}]", externalUserId);

        List<PocketPostRequest> pockets = pocketsReader.load();
        PocketPostRequest pocketPostRequest = pockets.get(counter);

        Pocket pocket = pocketsRestClient.ingestPocket(pocketPostRequest, counter);
        log.info("Pocket with ID [{}], arrangementID [{}] and name [{}] created for user [{}]",
            pocket.getId(),
            pocket.getArrangementId(),
            pocket.getName(),
            externalUserId);
    }

    private ArrangementAddedResponse ingestParentPocketArrangement(LegalEntityBase legalEntity) {
        PostArrangement parentPocketArrangement = ProductSummaryDataGenerator
            .generateParentPocketArrangement(legalEntity.getExternalId());
        return arrangementsIntegrationRestClient
            .ingestPocketArrangementAndLogResponse(parentPocketArrangement, EXTERNAL_ARRANGEMENT_ORIGINATION_1, true);
    }

    private ArrangementAddedResponse ingestPocketArrangement(LegalEntityBase legalEntity, int counter) {
        String externalArrangementId = EXTERNAL_ARRANGEMENT_ORIGINATION + counter;
        PostArrangement childPostArrangement = ProductSummaryDataGenerator
            .generateChildPocketArrangement(legalEntity.getExternalId(), externalArrangementId, counter);
        return arrangementsIntegrationRestClient
            .ingestPocketArrangementAndLogResponse(childPostArrangement, externalArrangementId, false);
    }

    private void updateDataGroupForPockets(String pocketArrangementId, LegalEntityBase legalEntity) {

        String dataGroupId = accessGroupService.updateDataGroup(pocketArrangementId,
            getExternalServiceAgreementId(legalEntity));

        log.debug("Updated data group with id [{}]", dataGroupId);
    }

    private String getExternalServiceAgreementId(
        com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase legalEntity) {
        String internalServiceAgreementId = this.legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .getId();
        return this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();
    }
}
