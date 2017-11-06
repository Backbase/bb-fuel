package com.backbase.testing.dataloader.setup;

import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;

import java.io.IOException;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;

public class BankSetup {

    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(EXTERNAL_ROOT_LEGAL_ENTITY_ID, USER_ADMIN);
        productSummaryConfigurator.ingestProducts();
    }
}
