package com.backbase.testing.dataloader.setup;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.PermissionsConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.testing.dataloader.configurators.TransactionsConfigurator;
import com.backbase.testing.dataloader.dto.ArrangementId;

import java.util.ArrayList;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class UsersSetup {

    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    private PermissionsConfigurator permissionsConfigurator =  new PermissionsConfigurator();
    private TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();
    private LegalEntitiesAndUsersConfigurator  legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();

    public void setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(List<String> externalUserIds) {
        legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
        loginRestClient.login(USER_ADMIN, USER_ADMIN);

        for (String externalUserId : externalUserIds) {

            LegalEntityByUserGetResponseBody legalEntity = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class);

            String externalLegalEntityId = legalEntity.getExternalId();
            String internalLegalEntityId = legalEntity.getId();

            List<ArrangementId> arrangementIds = productSummaryConfigurator.ingestArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId);
            List<String> internalArrangementIds = new ArrayList<>();

            arrangementIds.forEach(arrangementId -> internalArrangementIds.add(arrangementId.getInternalArrangementId()));

            FunctionAccessGroupsGetResponseBody[] functionGroups = accessGroupPresentationRestClient.retrieveFunctionGroupsByLegalEntity(internalLegalEntityId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(FunctionAccessGroupsGetResponseBody[].class);

            if (functionGroups.length == 0) {
                accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesForAllFunctions(externalLegalEntityId);
            }

            accessGroupsConfigurator.ingestDataGroupForArrangements(externalLegalEntityId, internalArrangementIds);
            permissionsConfigurator.assignAllFunctionDataGroupsOfLegalEntityToUserAndServiceAgreement(externalLegalEntityId, externalUserId, null);

            for (ArrangementId arrangementId : arrangementIds) {
                transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId());
            }
        }
    }
}
