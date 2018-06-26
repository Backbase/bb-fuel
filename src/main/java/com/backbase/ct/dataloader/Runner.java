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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    private final AccessControlSetup accessControlSetup;
    private final ServiceAgreementsSetup serviceAgreementsSetup;
    private final CapabilitiesDataSetup capabilitiesDataSetup;
    private final AccessControlHealthCheck accessControlHealthCheck;
    private final ProductSummaryHealthCheck productSummaryHealthCheck;
    private final TransactionsHealthCheck transactionsHealthCheck;

    @Override
    public void run(ApplicationArguments args) {
        try {
            doIt();
            System.exit(0);
        } catch (IOException e) {
            LOGGER.error("Failed setting up access", e);
            System.exit(1);
        }
    }

    /**
     * Sponsored runner.
     *
     * @throws IOException when setupAccess throws it
     */
    private void doIt() throws IOException {
        performHealthChecks();

        Instant start = Instant.now();

        setupAccess();
        ingestData();

        logDuration(start);
    }

    private void performHealthChecks() {
        accessControlHealthCheck.checkAccessControlServicesHealth();
        productSummaryHealthCheck.checkProductSummaryServicesHealth();
        transactionsHealthCheck.checkTransactionsServicesHealth();
    }

    private void setupAccess() throws IOException {
        accessControlSetup.setupBankWithEntitlementsAdminAndProducts();
        accessControlSetup.setupAccessControlForUsers();
        serviceAgreementsSetup.setupCustomServiceAgreements();
    }

    private void ingestData() {
        capabilitiesDataSetup.ingestBankNotifications();
        capabilitiesDataSetup.ingestContactsPerUser();
        capabilitiesDataSetup.ingestPaymentsPerUser();
        capabilitiesDataSetup.ingestConversationsPerUser();
        capabilitiesDataSetup.ingestActionsPerUser();
    }

    private void logDuration(Instant start) {
        Instant end = Instant.now();
        long totalSeconds = Duration.between(start, end).getSeconds();
        LOGGER.info("Time to ingest data was {} minutes and {} seconds", totalSeconds / 60, totalSeconds % 60);
    }
}