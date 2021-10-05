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
     * @param legalEntity legal entity
     * @return pocket parent arrangement id as String
     */
    public String ingestPocketParentArrangementAndSetEntitlements(LegalEntityBase legalEntity) {
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
     * Ingests Pockets for the given user.
     *
     * @param externalUserId External user ID to ingest pockets for.
     */
    public void ingestPockets(String externalUserId) {
        log.debug("Going to ingest pockets for user [{}]", externalUserId);

        List<PocketPostRequest> pockets = pocketsReader.load();

        for (PocketPostRequest pocketPostRequest : pockets) {
            Pocket pocket = pocketsRestClient.ingestPocket(pocketPostRequest);
            log.info("Pocket with ID [{}] and name [{}] created for user [{}]", pocket.getId(), pocket.getName(),
                externalUserId);
        }
    }

    private ArrangementAddedResponse ingestParentPocketArrangement(LegalEntityBase legalEntity) {
        PostArrangement parentPocketArrangement = ProductSummaryDataGenerator
            .generateParentPocketArrangement(legalEntity.getExternalId());
        return arrangementsIntegrationRestClient
            .ingestParentPocketArrangementAndLogResponse(parentPocketArrangement);
    }

    private void updateDataGroupForPockets(String parentPocketArrangementId, LegalEntityBase legalEntity) {

        String dataGroupId = accessGroupService.updateDataGroup(parentPocketArrangementId,
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