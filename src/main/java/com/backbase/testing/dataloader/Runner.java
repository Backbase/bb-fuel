package com.backbase.testing.dataloader;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.*;
import com.backbase.testing.dataloader.utils.ParserUtil;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ProductSummaryByLegalEntityIdGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.*;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.USERS_JSON;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class Runner {

    public static void main(String[] args) throws IOException {

        LoginRestClient loginRestClient = new LoginRestClient();
        LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
        UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
        AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
        AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
        ProductSummaryPresentationRestClient productSummaryPresentationRestClient = new ProductSummaryPresentationRestClient();
        PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();
        ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
        TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();

        List<String> externalUserIds = new ArrayList<>();
        List<HashMap<String, List<String>>> userLists = ParserUtil.convertJsonToObject(USERS_JSON, new TypeReference<List<HashMap<String, List<String>>>>() {});

        userLists.forEach(userList -> externalUserIds.addAll(userList.get("users")));

        legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(EXTERNAL_ROOT_LEGAL_ENTITY_ID, USER_ADMIN);

        for (HashMap<String, List<String>> userList : userLists) {
            List<String> users = userList.get("users");

            legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(users, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
        }

        productSummaryConfigurator.ingestProducts();

        for (String externalUserId : externalUserIds) {
            LegalEntityByUserGetResponseBody legalEntity = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class);

            String externalLegalEntityId = legalEntity.getExternalId();
            String internalLegalEntityId = legalEntity.getId();

            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            List<String> internalArrangementIds = productSummaryConfigurator.ingestArrangementsByLegalEntityAndReturnInternalArrangementIds(externalLegalEntityId);

            FunctionAccessGroupsGetResponseBody[] functionGroups = accessGroupPresentationRestClient.retrieveFunctionGroupsByLegalEntity(internalLegalEntityId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(FunctionAccessGroupsGetResponseBody[].class);

            if (functionGroups.length == 0) {
                accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesForAllFunctions(externalLegalEntityId);
            }

            accessGroupsConfigurator.ingestDataGroupForArrangements(externalLegalEntityId, internalArrangementIds);
            permissionsConfigurator.assignAllFunctionDataGroupsToMasterServiceAgreementAndUser(externalLegalEntityId, externalUserId);

            loginRestClient.login(externalUserId, externalUserId);

            ProductSummaryByLegalEntityIdGetResponseBody[] arrangements = productSummaryPresentationRestClient.getProductSummaryArrangements()
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(ProductSummaryByLegalEntityIdGetResponseBody[].class);

            for (ProductSummaryByLegalEntityIdGetResponseBody arrangement : arrangements) {
                transactionsConfigurator.ingestTransactionsByArrangement(arrangement.getExternalArrangementId());
            }
        }
    }
}
