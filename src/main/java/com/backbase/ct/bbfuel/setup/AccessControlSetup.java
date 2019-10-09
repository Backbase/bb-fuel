package com.backbase.ct.bbfuel.setup;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_ACCESS_CONTROL;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_BATCHES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_CONTACTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_BALANCE_HISTORY;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.bbfuel.enrich.LegalEntityWithUsersEnricher.createRootLegalEntityWithAdmin;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.configurator.AccessGroupsConfigurator;
import com.backbase.ct.bbfuel.configurator.LegalEntitiesAndUsersConfigurator;
import com.backbase.ct.bbfuel.configurator.PermissionsConfigurator;
import com.backbase.ct.bbfuel.configurator.ProductSummaryConfigurator;
import com.backbase.ct.bbfuel.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.bbfuel.configurator.TransactionsConfigurator;
import com.backbase.ct.bbfuel.dto.ArrangementId;
import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.UserContext;
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.enrich.ProductGroupSeedEnricher;
import com.backbase.ct.bbfuel.input.JobProfileReader;
import com.backbase.ct.bbfuel.input.LegalEntityWithUsersReader;
import com.backbase.ct.bbfuel.input.ProductGroupSeedReader;
import com.backbase.ct.bbfuel.input.validation.ProductGroupAssignmentValidator;
import com.backbase.ct.bbfuel.service.JobProfileService;
import com.backbase.ct.bbfuel.service.LegalEntityService;
import com.backbase.ct.bbfuel.service.ProductGroupService;
import com.backbase.ct.bbfuel.service.UserContextService;
import com.backbase.ct.bbfuel.config.MultiTenancyConfig;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.IntegrationIdentifier;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.IntegrationFunctionGroupDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessControlSetup extends BaseSetup {


    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;
    private final LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final ProductSummaryConfigurator productSummaryConfigurator;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;
    private final PermissionsConfigurator permissionsConfigurator;
    private final TransactionsConfigurator transactionsConfigurator;
    private final LegalEntityWithUsersReader legalEntityWithUsersReader;
    private final JobProfileService jobProfileService;
    private final ProductGroupService productGroupService;
    private final ProductGroupAssignmentValidator productGroupAssignmentValidator;
    private final ProductGroupSeedEnricher productGroupEnricher;
    private final UserContextService userContextService;
    private final LoginRestClient loginRestClient;
    private final JobProfileReader jobProfileReader;
    private final ProductGroupSeedReader productGroupSeedReader;
    private final LegalEntityService legalEntityService;
    @Getter
    private List<LegalEntityWithUsers> legalEntitiesWithUsers;
    @Setter
    private List<JobProfile> jobProfileTemplates;
    private List<ProductGroupSeed> productGroupSeedTemplates;

    public List<LegalEntityWithUsers> getLegalEntitiesWithUsersExcludingSupport() {
        return getLegalEntitiesWithUsers()
            .stream()
            .filter(legalEntities -> legalEntities.getUsers()
                .stream()
                .noneMatch(user -> "support".equals(user.getRole())))
            .collect(Collectors.toList());
    }

    /**
     * Prepare the environment before ingesting the entities.
     */
    public void prepare(String legalEntityWithUsersResource) {
        log.info("Loading legal entities with users {}", legalEntityWithUsersResource);
        this.legalEntitiesWithUsers = this.legalEntityWithUsersReader.load(legalEntityWithUsersResource);
        this.jobProfileTemplates = this.jobProfileReader.load();
        loadProductGroups();
        if (MultiTenancyConfig.isMultiTenancyEnvironment()) {
            // tenant admin user is in the first LE of the m10y file
            LegalEntityWithUsers tenant = legalEntitiesWithUsers.get(0);
            User admin = tenant.getUsers().stream()
                .filter(user -> user.getRole().equalsIgnoreCase("admin"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Legal entity does not have a bank admin"));
            legalEntityService.setRootAdmin(admin.getExternalId());
            MultiTenancyConfig.setTenantId(tenant.getTenantId());
            tenant.getUsers().remove(admin);
        } else {
            legalEntityService.setRootAdmin(globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN));
        }
    }

    /**
     * Legal entities, job profiles and product groups are loaded from files.
     */
    public void initiate() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            this.setupBankWithEntitlementsAdminAndProducts();
            this.setupAccessControlForUsers();
        } else if (this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS)
            || this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS)
            || this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_BATCHES)) {
            this.prepareJobProfiles();
        }
    }

    private void loadProductGroups() {
        this.productGroupSeedTemplates = this.productGroupSeedReader.load();
        this.productGroupAssignmentValidator.verify(this.legalEntitiesWithUsers, this.productGroupSeedTemplates);
        this.productGroupEnricher.enrichLegalEntitiesWithUsers(
            this.legalEntitiesWithUsers, this.productGroupSeedTemplates);
    }

    private void setupBankWithEntitlementsAdminAndProducts() {
        LegalEntityWithUsers rootBank = createRootLegalEntityWithAdmin(legalEntityService.getRootAdmin());
        this.productGroupEnricher.enrichLegalEntitiesWithUsers(
            singletonList(rootBank), this.productGroupSeedTemplates);

        this.legalEntitiesAndUsersConfigurator.ingestLegalEntityWithUsers(rootBank);
        this.productSummaryConfigurator.ingestProducts();
        assembleFunctionDataGroupsAndPermissions(rootBank);
    }

    private void setupAccessControlForUsers() {
        this.legalEntitiesWithUsers.forEach(legalEntity -> {
            this.legalEntitiesAndUsersConfigurator.ingestLegalEntityWithUsers(legalEntity);
            assembleFunctionDataGroupsAndPermissions(legalEntity);
        });
    }

    private Multimap<String, UserContext> createLegalEntitiesUserContextMap(
        LegalEntityWithUsers legalEntityWithUsers) {

        Multimap<String, UserContext> legalEntitiesUserContextMap = ArrayListMultimap.create();
        this.loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        legalEntityWithUsers.getUsers().forEach(user -> {
            LegalEntityByUserGetResponseBody legalEntity = this.userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(user.getExternalId());
            final String legalEntityExternalId = legalEntity.getExternalId();

            if (!legalEntitiesUserContextMap.containsKey(legalEntityExternalId)) {
                this.serviceAgreementsConfigurator
                    .updateMasterServiceAgreementWithExternalIdByLegalEntity(legalEntityExternalId);
            }

            UserContext userContext = userContextService.getUserContextBasedOnMSAByExternalUserId(user, legalEntity);
            legalEntitiesUserContextMap.put(userContext.getExternalLegalEntityId(), userContext);
        });

        return legalEntitiesUserContextMap;
    }

    private void assembleFunctionDataGroupsAndPermissions(LegalEntityWithUsers legalEntityWithUsers) {
        Multimap<String, UserContext> legalEntitiesUserContextMap =
            createLegalEntitiesUserContextMap(legalEntityWithUsers);
        this.loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        AtomicBoolean isOnce = new AtomicBoolean(true);

        legalEntitiesUserContextMap.values()
            .forEach(userContext -> {
                boolean isRetail = legalEntityWithUsers.getCategory().isRetail();
                if (isOnce.get()) {
                    ingestDataGroupArrangementsForServiceAgreement(userContext.getInternalServiceAgreementId(),
                        userContext.getExternalServiceAgreementId(),
                        userContext.getExternalLegalEntityId(),
                        legalEntityWithUsers.getCategory().isRetail());
                    ingestFunctionGroups(userContext.getExternalServiceAgreementId(), isRetail);
                    isOnce.getAndSet(false);
                }

                assignPermissions(userContext.getUser(),
                    userContext.getExternalServiceAgreementId(),
                    isRetail);
            });
    }

    protected void ingestDataGroupArrangementsForServiceAgreement(String internalServiceAgreementId,
        String externalServiceAgreementId,
        String externalLegalEntityId, boolean isRetail) {

        productGroupSeedTemplates.forEach(productGroupTemplate -> {
            ProductGroupSeed productGroupSeed = new ProductGroupSeed(productGroupTemplate);

            if ((isRetail && !productGroupSeed.getIsRetail()) || (!isRetail && productGroupSeed.getIsRetail())) {
                return;
            }

            // Combination of data group name and service agreement is unique in the system
            DataGroupsGetResponseBody existingDataGroup = accessGroupPresentationRestClient
                .retrieveDataGroupsByServiceAgreement(internalServiceAgreementId)
                .stream()
                .filter(dataGroupsGetResponseBody -> productGroupSeed.getProductGroupName()
                    .equals(dataGroupsGetResponseBody.getName()))
                .findFirst()
                .orElse(null);

            if (existingDataGroup == null) {
                List<ArrangementId> arrangementIds = this.productSummaryConfigurator.ingestArrangements(
                    externalLegalEntityId, productGroupSeed);

                productGroupSeed.setExternalServiceAgreementId(externalServiceAgreementId);
                this.accessGroupsConfigurator.ingestDataGroupForArrangements(productGroupSeed, arrangementIds);

                ingestTransactions(arrangementIds, isRetail);
                ingestBalanceHistory(arrangementIds);
            } else {
                productGroupSeed.setId(existingDataGroup.getId());
                productGroupSeed.setExternalServiceAgreementId(externalServiceAgreementId);
                productGroupService.saveAssignedProductGroup(productGroupSeed);
            }
        });
    }

    private void ingestTransactions(List<ArrangementId> arrangementIds, boolean isRetail) {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            arrangementIds.parallelStream()
                .forEach(arrangementId -> this.transactionsConfigurator
                    .ingestTransactionsByArrangement(arrangementId.getExternalArrangementId(), isRetail));
        }
    }

    private void ingestBalanceHistory(List<ArrangementId> arrangementIds) {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_BALANCE_HISTORY)) {
            arrangementIds.parallelStream()
                .forEach(arrangementId -> this.productSummaryConfigurator
                    .ingestBalanceHistory(arrangementId.getExternalArrangementId()));
        }
    }

    /**
     * This will populate the jobProfiles in the local JobProfileService even if ingested already.
     */
    private void prepareJobProfiles() {
        this.legalEntitiesWithUsers.forEach(legalEntityWithUsers -> {
            boolean isRetail = legalEntityWithUsers.getCategory().isRetail();
            createLegalEntitiesUserContextMap(legalEntityWithUsers)
                .values()
                .forEach(userContext -> ingestFunctionGroups(
                    userContext.getExternalServiceAgreementId(), isRetail)
                );
        });
    }

    /**
     * AccessGroupConfigurator is called to ingest and detects duplicates.
     */
    private void ingestFunctionGroups(String externalServiceAgreementId, boolean isRetail) {
        if (this.jobProfileService.getAssignedJobProfiles(externalServiceAgreementId) == null) {
            jobProfileTemplates.forEach(template -> {
                if (!jobProfileService.isJobProfileForBranch(isRetail, template)) {
                    log.info("Job profile template [{}] does not apply to this legal entity [isRetail: {}]",
                        template.getJobProfileName(), isRetail);
                    return;
                }
                JobProfile jobProfile = new JobProfile(template);
                jobProfile.setExternalServiceAgreementId(externalServiceAgreementId);
                this.accessGroupsConfigurator.ingestFunctionGroup(jobProfile);
                jobProfileService.saveAssignedProfile(jobProfile);
            });
        }
    }

    private void assignPermissions(User user,
        String externalServiceAgreementId, boolean isRetail) {
        List<IntegrationFunctionGroupDataGroup> functionGroupDataGroups = new ArrayList<>();

        this.jobProfileService.getAssignedJobProfiles(externalServiceAgreementId).forEach(jobProfile -> {
            if (jobProfileService.isJobProfileForUserRole(jobProfile, user.getRole(), isRetail)) {

                List<String> dataGroupIds = this.productGroupService
                    .findAssignedProductGroupsIds(externalServiceAgreementId, user);
                List<IntegrationIdentifier> dataGroupIdentifiers = new ArrayList<>();

                dataGroupIds.forEach(dataGroupId -> dataGroupIdentifiers.add(new IntegrationIdentifier().withIdIdentifier(dataGroupId)));

                functionGroupDataGroups.add(new IntegrationFunctionGroupDataGroup()
                    .withFunctionGroupIdentifier(
                        new IntegrationIdentifier().withIdIdentifier(jobProfile.getId()))
                    .withDataGroupIdentifiers(dataGroupIdentifiers));
            }
        });
        this.permissionsConfigurator.assignPermissions(
            user.getExternalId(), externalServiceAgreementId, functionGroupDataGroups);


    }
}
