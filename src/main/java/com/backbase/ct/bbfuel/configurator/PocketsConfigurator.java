package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
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
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

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

        // TODO problem to solve:
        // Selected is data in the data group defined in retail/product-group-seed.json by groupname
        // 'Retail Accounts U.S'
        // for user defined in retail/legal-entities-with-users.json by role 'retail'
        // For permissions to work we have to ingest datagroup with the parent pocket arrangement
        // (from response.getId())
        // and with the existing groupname 'Retail Accounts U.S'.
        // This should result in entries in the table accesscontrol_pandp.data_group_item for the datagroup associated
        // with the groupname and the parent pocket arrangement.
        //
        // BUT inserting with the same groupname result in an duplicate error, see access control logs!!!
        // inserting by hand when halting with breakpoint will work and pockets will be created
        //
        // insert into accesscontrol_pandp.data_group_item (data_group_id, data_item_id)
        // values ('existing data group id', 'parent pocket arrangement id');

        String internalServiceAgreementId = this.legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .getId();
        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();
        List<String> internalArrangementIds = new ArrayList<>();
        internalArrangementIds.add(response.getId());
        String dataGroupId = accessGroupService
            .ingestDataGroup(externalServiceAgreementId, "Retail Accounts U.S", "ARRANGEMENTS", internalArrangementIds);
        log.debug("created data group with id [{}]", dataGroupId);

        // TODO you probably do not have to ingest function group if above data group is correctly ingested
        log.debug("Going to ingest function group and use hardcoded 'Manage Pockets' as function_group.name");
        List<FunctionsGetResponseBody> bodyList = accessGroupIntegrationRestClient.retrieveFunctions();
        List<Privilege> privileges = new ArrayList<>();
        privileges.add(new Privilege().withPrivilege("create"));
        privileges.add(new Privilege().withPrivilege("edit"));
        privileges.add(new Privilege().withPrivilege("delete"));
        privileges.add(new Privilege().withPrivilege("execute"));
        privileges.add(new Privilege().withPrivilege("view"));
        List<Permission> permissions = new ArrayList<>();
        List<String> functionIds = bodyList.stream()
            .filter(functionsGetResponseBody -> functionsGetResponseBody.getResource()
                .equalsIgnoreCase("Personal Finance Management"))
            .map(FunctionsGetResponseBody::getFunctionId)
            .collect(Collectors.toList());
        permissions.add(new Permission().withFunctionId(functionIds.get(0)).withAssignedPrivileges(privileges));
        String functionGroupId = accessGroupService
            .ingestFunctionGroup(externalServiceAgreementId, "Manage Pockets", "REGULAR", permissions);
        log.debug("created function group with id {}", functionGroupId);

        return response;
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
}
