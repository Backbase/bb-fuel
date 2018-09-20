package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.ArrangementType.FINANCE_INTERNATIONAL;
import static com.backbase.ct.dataloader.data.ArrangementType.GENERAL_BUSINESS;
import static com.backbase.ct.dataloader.data.ArrangementType.GENERAL_RETAIL;
import static com.backbase.ct.dataloader.data.ArrangementType.INTERNATIONAL_TRADE;
import static com.backbase.ct.dataloader.data.ArrangementType.PAYROLL;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_ACCESS_CONTROL;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_BALANCE_HISTORY;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_INTERNATIONAL_AND_PAYROLL_DATA_GROUPS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.dataloader.enrich.LegalEntityWithUsersEnricher.createAdminUser;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import static java.util.Collections.singletonList;

import com.backbase.ct.dataloader.IngestException;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.configurator.AccessGroupsConfigurator;
import com.backbase.ct.dataloader.configurator.LegalEntitiesAndUsersConfigurator;
import com.backbase.ct.dataloader.configurator.PermissionsConfigurator;
import com.backbase.ct.dataloader.configurator.ProductSummaryConfigurator;
import com.backbase.ct.dataloader.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.dataloader.configurator.TransactionsConfigurator;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.dto.DataGroupCollection;
import com.backbase.ct.dataloader.dto.LegalEntityContext;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.User;
import com.backbase.ct.dataloader.dto.UserContext;
import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.input.JobProfileReader;
import com.backbase.ct.dataloader.input.LegalEntityWithUsersReader;
import com.backbase.ct.dataloader.service.JobProfileService;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessControlSetup extends BaseSetup {

    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final ProductSummaryConfigurator productSummaryConfigurator;
    private final AccessGroupsConfigurator accessGroupsConfigurator;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;
    private final PermissionsConfigurator permissionsConfigurator;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final TransactionsConfigurator transactionsConfigurator;
    private final LegalEntityWithUsersReader legalEntityWithUsersReader;
    private final JobProfileService jobProfileService;

    private final JobProfileReader jobProfileReader;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
    private List<LegalEntityWithUsers> legalEntitiesWithUsers;
    private List<JobProfile> jobProfileTemplates;

    public List<LegalEntityWithUsers> getLegalEntitiesWithUsers() {
        return this.legalEntitiesWithUsers;
    }

    /**
     * Legal entities are loaded from file.
     */
    public void initiate() throws IOException {
        this.legalEntitiesWithUsers = this.legalEntityWithUsersReader.load();
        this.jobProfileTemplates = this.jobProfileReader.load();
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            this.setupBankWithEntitlementsAdminAndProducts();
            this.setupAccessControlForUsers();
        }
    }

    private void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        User admin = createAdminUser(rootEntitlementsAdmin);
        this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(admin);
        this.productSummaryConfigurator.ingestProducts();
        assembleFunctionDataGroupsAndPermissions(singletonList(admin));
    }

    private void setupAccessControlForUsers() {
        this.legalEntitiesWithUsers.forEach(entity -> {
            this.legalEntitiesAndUsersConfigurator.ingestUsersUnderLegalEntity(entity);
            assembleFunctionDataGroupsAndPermissions(entity.getUsers());
        });
    }

    private void assembleFunctionDataGroupsAndPermissions(List<User> users) {
        Multimap<String, UserContext> legalEntitiesUserContextMap = ArrayListMultimap.create();
        final LegalEntityContext legalEntityContext = new LegalEntityContext();
        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        users.forEach(user -> {
            LegalEntityByUserGetResponseBody legalEntity = this.userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(user.getExternalId());
            final String legalEntityExternalId = legalEntity.getExternalId();

            if (!legalEntitiesUserContextMap.containsKey(legalEntityExternalId)) {
                this.serviceAgreementsConfigurator
                    .updateMasterServiceAgreementWithExternalIdByLegalEntity(legalEntityExternalId);
            }

            UserContext userContext = getUserContextBasedOnMSAByExternalUserId(user, legalEntity);
            legalEntitiesUserContextMap.put(userContext.getExternalLegalEntityId(), userContext);

            if (legalEntityContext.getDataGroupCollection() == null) {
                legalEntityContext.setDataGroupCollection(
                    ingestDataGroupArrangementsForServiceAgreement(userContext.getExternalServiceAgreementId(),
                        userContext.getExternalLegalEntityId(), users.size()));
            }
        });

        legalEntitiesUserContextMap.values()
            .forEach(userContext -> ingestFunctionGroupsAndAssignPermissions(userContext.getUser(),
                userContext.getInternalServiceAgreementId(),
                userContext.getExternalServiceAgreementId(),
                legalEntityContext.getDataGroupCollection()));
    }

    protected DataGroupCollection ingestDataGroupArrangementsForServiceAgreement(String externalServiceAgreementId,
        String externalLegalEntityId, int numberOfUsersInServiceAgreement) {
        final DataGroupCollection dataGroupCollection = new DataGroupCollection();
        List<Callable<Void>> taskList = new ArrayList<>();

        if (numberOfUsersInServiceAgreement == 1) {
            taskList.add(() -> generateTask(externalServiceAgreementId, "General EUR",
                () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, GENERAL_RETAIL, Currency.EUR
                ),
                dataGroupCollection::setGeneralEurId));
            taskList.add(() -> generateTask(externalServiceAgreementId, "General USD",
                () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, GENERAL_RETAIL, Currency.USD
                ),
                dataGroupCollection::setGeneralUsdId));
        } else {
            taskList.add(() -> generateTask(externalServiceAgreementId, "Amsterdam",
                () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, GENERAL_BUSINESS,
                    Currency.EUR
                ),
                dataGroupCollection::setAmsterdamId));

            taskList.add(() -> generateTask(externalServiceAgreementId, "Portland",
                () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, GENERAL_BUSINESS,
                    Currency.USD
                ),
                dataGroupCollection::setPortlandId));

            taskList.add(() -> generateTask(externalServiceAgreementId, "Vancouver",
                () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, GENERAL_BUSINESS,
                    Currency.CAD
                ),
                dataGroupCollection::setVancouverId));

            taskList.add(() -> generateTask(externalServiceAgreementId, "London",
                () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, GENERAL_BUSINESS,
                    Currency.GBP
                ),
                dataGroupCollection::setLondonId));

            if (this.globalProperties.getBoolean(PROPERTY_INGEST_INTERNATIONAL_AND_PAYROLL_DATA_GROUPS)) {
                taskList.add(() -> generateTask(externalServiceAgreementId, INTERNATIONAL_TRADE.toString(),
                    () -> this.productSummaryConfigurator
                        .ingestArrangements(externalLegalEntityId, INTERNATIONAL_TRADE, null),
                    dataGroupCollection::setInternationalTradeId));

                taskList.add(() -> generateTask(externalServiceAgreementId, FINANCE_INTERNATIONAL.toString(),
                    () -> this.productSummaryConfigurator
                        .ingestArrangements(externalLegalEntityId, FINANCE_INTERNATIONAL, null),
                    dataGroupCollection::setFinanceInternationalId));

                taskList.add(() -> generateTask(externalServiceAgreementId, PAYROLL.toString(),
                    () -> this.productSummaryConfigurator.ingestArrangements(externalLegalEntityId, PAYROLL, null),
                    dataGroupCollection::setPayrollId));
            }
        }

        taskList.parallelStream()
            .forEach(voidCallable -> {
                try {
                    voidCallable.call();
                } catch (Exception e) {
                    throw new IngestException(e.getMessage(), e);
                }
            });

        return dataGroupCollection;
    }

    private void ingestFunctionGroupsAndAssignPermissions(User user, String internalServiceAgreementId,
        String externalServiceAgreementId,
        DataGroupCollection dataGroupCollection) {

        if (this.jobProfileService.getAssignedJobProfiles(externalServiceAgreementId) == null) {
            jobProfileTemplates.forEach(template -> {
                JobProfile jobProfile = new JobProfile(template);
                jobProfile.setExternalServiceAgreementId(externalServiceAgreementId);
                this.accessGroupsConfigurator.ingestFunctionGroup(jobProfile);
                jobProfileService.saveAssignedProfile(jobProfile);
            });
        }

        this.jobProfileService.getAssignedJobProfiles(externalServiceAgreementId).forEach(jobProfile -> {
            if (jobProfileService.isJobProfileForUserRole(jobProfile, user.getRole())) {
                this.permissionsConfigurator.assignPermissions(
                    user.getExternalId(),
                    internalServiceAgreementId,
                    jobProfile.getId(),
                    dataGroupCollection.getDataGroupIds());
            }
        });
    }

    public UserContext getUserContextBasedOnMSAByExternalUserId(User user) {
        return getUserContextBasedOnMSAByExternalUserId(user, null);
    }

    private UserContext getUserContextBasedOnMSAByExternalUserId(User user,
            LegalEntityByUserGetResponseBody legalEntity) {
        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalUserId = this.userPresentationRestClient.getUserByExternalId(user.getExternalId()).getId();

        if (legalEntity == null) {
            legalEntity = this.userPresentationRestClient.retrieveLegalEntityByExternalUserId(user.getExternalId());
        }

        String internalServiceAgreementId = this.legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .getId();

        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();

        return new UserContext()
            .withUser(user)
            .withInternalUserId(internalUserId)
            .withInternalServiceAgreementId(internalServiceAgreementId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withInternalLegalEntityId(legalEntity.getId())
            .withExternalLegalEntityId(legalEntity.getExternalId());
    }

    private Void generateTask(String externalServiceAgreementId, String dataGroupName,
        Supplier<List<ArrangementId>> supplier,
        Consumer<String> consumer) {
        List<ArrangementId> arrangementIds = new ArrayList<>(supplier.get());
        String dataGroupId = this.accessGroupsConfigurator
            .ingestDataGroupForArrangements(externalServiceAgreementId, dataGroupName, arrangementIds);

        if (this.globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            arrangementIds.parallelStream()
                .forEach(arrangementId -> this.transactionsConfigurator
                    .ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
        }

        if (this.globalProperties.getBoolean(PROPERTY_INGEST_BALANCE_HISTORY)) {
            arrangementIds.parallelStream()
                .forEach(arrangementId -> this.productSummaryConfigurator
                    .ingestBalanceHistory(arrangementId.getExternalArrangementId()));
        }

        consumer.accept(dataGroupId);
        return null;
    }
}
