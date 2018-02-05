package com.backbase.testing.dataloader.setup;

import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.NotificationsConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.testing.dataloader.utils.GlobalProperties;

import java.io.IOException;
import java.util.Collections;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

public class BankSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private NotificationsConfigurator notificationsConfigurator = new NotificationsConfigurator();
    private UsersSetup usersSetup = new UsersSetup();

    public BankSetup() throws IOException {
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(EXTERNAL_ROOT_LEGAL_ENTITY_ID, USER_ADMIN);
            productSummaryConfigurator.ingestProducts();

            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            usersSetup.setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(Collections.singletonList(USER_ADMIN));
        }
    }

    public void setupBankNotifications() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            notificationsConfigurator.ingestNotifications();
        }
    }
}
