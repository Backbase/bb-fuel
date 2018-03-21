package com.backbase.ct.dataloader.setup;

import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.ct.dataloader.configurators.NotificationsConfigurator;
import com.backbase.ct.dataloader.configurators.ProductSummaryConfigurator;

import java.io.IOException;
import java.util.Collections;

public class BankSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private NotificationsConfigurator notificationsConfigurator = new NotificationsConfigurator();
    private LegalEntitiesWithUsersSetup legalEntitiesWithUsersSetup = new LegalEntitiesWithUsersSetup();

    public BankSetup() throws IOException {
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_ENTITLEMENTS)) {
            this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(CommonConstants.USER_ADMIN);
            this.productSummaryConfigurator.ingestProducts();
            this.legalEntitiesWithUsersSetup.assembleUsersPrivilegesAndDataGroups(Collections.singletonList(CommonConstants.USER_ADMIN));
        }
    }

    public void setupBankNotifications() {
        if (this.globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_NOTIFICATIONS)) {
            this.loginRestClient.login(CommonConstants.USER_ADMIN, CommonConstants.USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.notificationsConfigurator.ingestNotifications();
        }
    }
}
