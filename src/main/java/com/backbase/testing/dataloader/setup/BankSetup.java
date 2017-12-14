package com.backbase.testing.dataloader.setup;

import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.NotificationsConfigurator;
import com.backbase.testing.dataloader.configurators.PermissionsConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.testing.dataloader.configurators.TransactionsConfigurator;
import com.backbase.testing.dataloader.dto.ArrangementId;
import com.backbase.testing.dataloader.utils.GlobalProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;

public class BankSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private NotificationsConfigurator notificationsConfigurator = new NotificationsConfigurator();
    private TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();

    public BankSetup() throws IOException {
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(EXTERNAL_ROOT_LEGAL_ENTITY_ID, USER_ADMIN);
            productSummaryConfigurator.ingestProducts();
            setupFunctionDataGroupsUnderRootLegalEntity();
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            permissionsConfigurator.assignAllFunctionDataGroupsOfLegalEntityToUserAndMasterServiceAgreement(EXTERNAL_ROOT_LEGAL_ENTITY_ID, USER_ADMIN);
        }
    }

    public void setupBankNotifications() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            notificationsConfigurator.ingestNotifications();
        }
    }

    private void setupFunctionDataGroupsUnderRootLegalEntity() {
        List<ArrangementId> arrangementIds = productSummaryConfigurator.ingestArrangementsByLegalEntityAndReturnArrangementIds(EXTERNAL_ROOT_LEGAL_ENTITY_ID);
        List<String> internalArrangementIds = new ArrayList<>();

        arrangementIds.forEach(arrangementId -> internalArrangementIds.add(arrangementId.getInternalArrangementId()));

        accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesForAllFunctions(EXTERNAL_ROOT_LEGAL_ENTITY_ID);
        accessGroupsConfigurator.ingestDataGroupForArrangements(EXTERNAL_ROOT_LEGAL_ENTITY_ID, internalArrangementIds);

        if (globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            for (ArrangementId arrangementId : arrangementIds) {
                transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId());
            }
        }
    }
}
