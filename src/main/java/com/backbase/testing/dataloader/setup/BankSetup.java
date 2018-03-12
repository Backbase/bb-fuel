package com.backbase.testing.dataloader.setup;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;

import com.backbase.testing.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.NotificationsConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import java.io.IOException;
import java.util.Collections;

public class BankSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private NotificationsConfigurator notificationsConfigurator = new NotificationsConfigurator();
    private LegalEntitiesWithUsersAssembler legalEntitiesWithUsersAssembler = new LegalEntitiesWithUsersAssembler();

    public BankSetup() throws IOException {
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(EXTERNAL_ROOT_LEGAL_ENTITY_ID, USER_ADMIN);
            this.productSummaryConfigurator.ingestProducts();
            this.legalEntitiesWithUsersAssembler.assembleUsersPrivilegesAndDataGroups(Collections.singletonList(USER_ADMIN));
        }
    }

    public void setupBankNotifications() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.notificationsConfigurator.ingestNotifications();
        }
    }
}
