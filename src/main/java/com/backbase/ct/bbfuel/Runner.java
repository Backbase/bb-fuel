package com.backbase.ct.bbfuel;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_M10Y_LEGAL_ENTITIES_WITH_USERS_JSON;

import com.backbase.ct.bbfuel.healthcheck.AccessControlHealthCheck;
import com.backbase.ct.bbfuel.healthcheck.BillPayHealthCheck;
import com.backbase.ct.bbfuel.healthcheck.ProductSummaryHealthCheck;
import com.backbase.ct.bbfuel.healthcheck.TransactionsHealthCheck;
import com.backbase.ct.bbfuel.setup.AccessControlSetup;
import com.backbase.ct.bbfuel.setup.CapabilitiesDataSetup;
import com.backbase.ct.bbfuel.setup.ServiceAgreementsSetup;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.ct.bbfuel.config.MultiTenancyConfig;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final AccessControlSetup accessControlSetup;
    private final ServiceAgreementsSetup serviceAgreementsSetup;
    private final CapabilitiesDataSetup capabilitiesDataSetup;
    private final AccessControlHealthCheck accessControlHealthCheck;
    private final ProductSummaryHealthCheck productSummaryHealthCheck;
    private final TransactionsHealthCheck transactionsHealthCheck;
    private final BillPayHealthCheck billPayHealthCheck;

    @Override
    public void run(ApplicationArguments args) {
        try {
            doIt();
            System.exit(0);
        } catch (IOException e) {
            log.error("Failed setting up access", e);
            System.exit(1);
        }
    }

    /**
     * Sponsored runner.
     *
     * @throws IOException when setupAccessControl throws it
     */
    private void doIt() throws IOException {
        if (log.isInfoEnabled()) {
            String environment = GlobalProperties.getInstance().getString("environment.name");
            log.info("Ingesting data into {}", (environment == null ? "environment" : environment));
        }

        Instant start = Instant.now();
        ingestEnvironment();
        logDuration(start);
    }

    private void ingestEnvironment() throws IOException {
        String[] tenants = new String[]{
            GlobalProperties.getInstance().getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON)};
        if (MultiTenancyConfig.isMultiTenancyEnvironment()) {
            String legalEntityResource = GlobalProperties.getInstance()
                .getString(PROPERTY_M10Y_LEGAL_ENTITIES_WITH_USERS_JSON);
            tenants = StringUtils.split(legalEntityResource, ";");
            if (tenants.length < 2) {
                log.error("Your multi-tenant environment needs at least 2 tenants, you configured only #{}: {}={}",
                    tenants.length, PROPERTY_M10Y_LEGAL_ENTITIES_WITH_USERS_JSON, legalEntityResource);
                return;
            }
        }

        for (String tenant : tenants) {
            accessControlSetup.prepare(tenant);
            performHealthChecks();
            setupAccessControl();
            ingestCapabilityData();
        }
    }

    private void performHealthChecks() {
        accessControlHealthCheck.checkAccessControlServicesHealth();
        productSummaryHealthCheck.checkProductSummaryServicesHealth();
        transactionsHealthCheck.checkTransactionsServicesHealth();
        billPayHealthCheck.checkBillPayServicesHealth();
    }

    private void setupAccessControl() throws IOException {
        accessControlSetup.initiate();
        serviceAgreementsSetup.initiate();
    }

    private void ingestCapabilityData() {
        capabilitiesDataSetup.initiate();
    }

    private void logDuration(Instant start) {
        Instant end = Instant.now();
        long totalSeconds = Duration.between(start, end).getSeconds();
        log.info("Time to ingest data was {} minutes and {} seconds", totalSeconds / 60, totalSeconds % 60);
    }
}
