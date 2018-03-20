package com.backbase.testing.dataloader;

import com.backbase.testing.dataloader.healthchecks.EntitlementsHealthCheck;
import com.backbase.testing.dataloader.healthchecks.ProductSummaryHealthCheck;
import com.backbase.testing.dataloader.healthchecks.TransactionsHealthCheck;
import com.backbase.testing.dataloader.setup.BankSetup;
import com.backbase.testing.dataloader.setup.LegalEntitiesWithUsersSetup;
import com.backbase.testing.dataloader.setup.ServiceAgreementsSetup;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

    public static void main(String[] args) throws IOException {
        final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8");

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

}
