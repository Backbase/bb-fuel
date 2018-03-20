package com.backbase.testing.dataloader;

import static com.backbase.testing.dataloader.data.CommonConstants.LOCAL_ENTITLEMENTS_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.LOCAL_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.LOCAL_INFRA_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.LOCAL_LOGIN_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.LOCAL_PRODUCT_SUMMARY_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.LOCAL_TRANSACTIONS_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONFIGURATION_SWITCHER;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LOGIN_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PRODUCT_SUMMARY_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_TRANSACTIONS_BASE_URI;

import com.backbase.testing.dataloader.healthchecks.EntitlementsHealthCheck;
import com.backbase.testing.dataloader.healthchecks.ProductSummaryHealthCheck;
import com.backbase.testing.dataloader.healthchecks.TransactionsHealthCheck;
import com.backbase.testing.dataloader.setup.BankSetup;
import com.backbase.testing.dataloader.setup.LegalEntitiesWithUsersSetup;
import com.backbase.testing.dataloader.setup.ServiceAgreementsSetup;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

    public static void main(String[] args) throws IOException {
        final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8");
        syncApplicationProperties();

        BankSetup bankSetup = new BankSetup();
        LegalEntitiesWithUsersSetup legalEntitiesWithUsersSetup = new LegalEntitiesWithUsersSetup();
        ServiceAgreementsSetup serviceAgreementsSetup = new ServiceAgreementsSetup();
        EntitlementsHealthCheck entitlementsHealthCheck = new EntitlementsHealthCheck();
        ProductSummaryHealthCheck productSummaryHealthCheck = new ProductSummaryHealthCheck();
        TransactionsHealthCheck transactionsHealthCheck = new TransactionsHealthCheck();

        entitlementsHealthCheck.checkEntitlementsServicesHealth();
        productSummaryHealthCheck.checkProductSummaryServicesHealth();
        transactionsHealthCheck.checkTransactionsServicesHealth();

        Instant start = Instant.now();

        bankSetup.setupBankWithEntitlementsAdminAndProducts();
        legalEntitiesWithUsersSetup.assembleUsersWithAndWithoutFunctionDataGroupsPrivileges();
        serviceAgreementsSetup.setupCustomServiceAgreements();

        bankSetup.setupBankNotifications();

        legalEntitiesWithUsersSetup.assembleContactsPerUser();
        legalEntitiesWithUsersSetup.assemblePaymentsPerUser();
        legalEntitiesWithUsersSetup.assembleConversationsPerUser();

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long minutes = duration.getSeconds() / 60;
        long seconds = duration.getSeconds() % 60;
        LOGGER.info("Time to ingest data was " + minutes + " minutes and " + seconds + " seconds");
    }

    private static void syncApplicationProperties() {
        final GlobalProperties globalProperties = GlobalProperties.getInstance();
        if (globalProperties.getBoolean(PROPERTY_CONFIGURATION_SWITCHER)) {
            globalProperties.setProperty(PROPERTY_INFRA_BASE_URI, LOCAL_INFRA_BASE_URI);
            globalProperties.setProperty(PROPERTY_GATEWAY_PATH, LOCAL_GATEWAY_PATH);
            globalProperties.setProperty(PROPERTY_LOGIN_PATH, LOCAL_LOGIN_PATH);
            globalProperties.setProperty(PROPERTY_ENTITLEMENTS_BASE_URI, LOCAL_ENTITLEMENTS_BASE_URI);
            globalProperties.setProperty(PROPERTY_PRODUCT_SUMMARY_BASE_URI, LOCAL_PRODUCT_SUMMARY_BASE_URI);
            globalProperties.setProperty(PROPERTY_TRANSACTIONS_BASE_URI, LOCAL_TRANSACTIONS_BASE_URI);
        }
    }
}
