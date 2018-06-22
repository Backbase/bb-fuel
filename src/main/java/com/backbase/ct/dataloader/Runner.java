package com.backbase.ct.dataloader;

import com.backbase.ct.dataloader.healthchecks.AccessControlHealthCheck;
import com.backbase.ct.dataloader.healthchecks.ProductSummaryHealthCheck;
import com.backbase.ct.dataloader.healthchecks.TransactionsHealthCheck;
import com.backbase.ct.dataloader.setup.AccessControlSetup;
import com.backbase.ct.dataloader.setup.CapabilitiesDataSetup;
import com.backbase.ct.dataloader.setup.ServiceAgreementsSetup;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

    public static void doit(String[] args) throws IOException {
        final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8");

        AccessControlSetup accessControlSetup = new AccessControlSetup();
        ServiceAgreementsSetup serviceAgreementsSetup = new ServiceAgreementsSetup();
        CapabilitiesDataSetup capabilitiesDataSetup = new CapabilitiesDataSetup();
        AccessControlHealthCheck accessControlHealthCheck = new AccessControlHealthCheck();
        ProductSummaryHealthCheck productSummaryHealthCheck = new ProductSummaryHealthCheck();
        TransactionsHealthCheck transactionsHealthCheck = new TransactionsHealthCheck();

        accessControlHealthCheck.checkAccessControlServicesHealth();
        productSummaryHealthCheck.checkProductSummaryServicesHealth();
        transactionsHealthCheck.checkTransactionsServicesHealth();

        Instant start = Instant.now();

        accessControlSetup.setupBankWithEntitlementsAdminAndProducts();
        accessControlSetup.setupAccessControlForUsers();
        serviceAgreementsSetup.setupCustomServiceAgreements();

        capabilitiesDataSetup.ingestBankNotifications();
        capabilitiesDataSetup.ingestContactsPerUser();
        capabilitiesDataSetup.ingestPaymentsPerUser();
        capabilitiesDataSetup.ingestConversationsPerUser();
        capabilitiesDataSetup.ingestActionsPerUser();

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long minutes = duration.getSeconds() / 60;
        long seconds = duration.getSeconds() % 60;
        LOGGER.info("Time to ingest data was " + minutes + " minutes and " + seconds + " seconds");
    }

}
