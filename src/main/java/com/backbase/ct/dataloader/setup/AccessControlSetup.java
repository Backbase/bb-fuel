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
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.dataloader.IngestException;
import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.configurator.AccessGroupsConfigurator;
import com.backbase.ct.dataloader.configurator.LegalEntitiesAndUsersConfigurator;
import com.backbase.ct.dataloader.configurator.ProductSummaryConfigurator;
import com.backbase.ct.dataloader.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.dataloader.configurator.TransactionsConfigurator;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.dto.DataGroupCollection;
import com.backbase.ct.dataloader.dto.LegalEntityContext;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.UserContext;
import com.backbase.ct.dataloader.input.LegalEntityWithUsersReader;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final TransactionsConfigurator transactionsConfigurator;
    private final LegalEntityWithUsersReader legalEntityWithUsersReader;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
    private List<LegalEntityWithUsers> legalEntitiesWithUsers;

    public List<LegalEntityWithUsers> getLegalEntitiesWithUsers() {
        return this.legalEntitiesWithUsers;
    }

    /**
     * Legal entities are loaded from file.
     */
    public void readLegalEntitiesWithUsers() {
        this.legalEntitiesWithUsers = this.legalEntityWithUsersReader.load();
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(rootEntitlementsAdmin);
            this.productSummaryConfigurator.ingestProducts();
            assembleFunctionDataGroupsAndPermissions(singletonList(rootEntitlementsAdmin));
        }
    }

    public void setupAccessControlForUsers() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            this.legalEntitiesWithUsers.forEach(entity -> {
                this.legalEntitiesAndUsersConfigurator.ingestUsersUnderLegalEntity(entity);
                assembleFunctionDataGroupsAndPermissions(entity.getUserExternalIds());
            });
        }
    }

    private void assembleFunctionDataGroupsAndPermissions(List<String> userExternalIds) {
        Multimap<String, UserContext> legalEntitiesUserContextMap = ArrayListMultimap.create();
        final LegalEntityContext legalEntityContext = new LegalEntityContext();
        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        userExternalIds.forEach(userExternalId -> {
            final String legalEntityExternalId = this.userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(userExternalId)
                .getExternalId();

            if (!legalEntitiesUserContextMap.containsKey(legalEntityExternalId)) {
                this.serviceAgreementsConfigurator
                    .updateMasterServiceAgreementWithExternalIdByLegalEntity(legalEntityExternalId);
            }

            UserContext userContext = getUserContextBasedOnMSAByExternalUserId(userExternalId);
            legalEntitiesUserContextMap.put(userContext.getExternalLegalEntityId(), userContext);

            if (legalEntityContext.getDataGroupCollection() == null) {
                legalEntityContext.setDataGroupCollection(
                    ingestDataGroupArrangementsForServiceAgreement(userContext.getExternalServiceAgreementId(),
                        userContext.getExternalLegalEntityId(), userExternalIds.size()));
            }
        });

        legalEntitiesUserContextMap.values()
            .forEach(userContext -> ingestFunctionGroupsAndAssignPermissions(userContext.getExternalUserId(),
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

    private void ingestFunctionGroupsAndAssignPermissions(String externalUserId, String internalServiceAgreementId,
        String externalServiceAgreementId,
        DataGroupCollection dataGroupCollection) {

        String adminFunctionGroupId = this.accessGroupsConfigurator
            .ingestAdminFunctionGroup(externalServiceAgreementId);

        List<String> dataGroupIds = asList(
            dataGroupCollection.getGeneralEurId(),
            dataGroupCollection.getGeneralUsdId(),
            dataGroupCollection.getAmsterdamId(),
            dataGroupCollection.getPortlandId(),
            dataGroupCollection.getVancouverId(),
            dataGroupCollection.getLondonId(),
            dataGroupCollection.getInternationalTradeId(),
            dataGroupCollection.getFinanceInternationalId(),
            dataGroupCollection.getPayrollId())
            .parallelStream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        this.accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            adminFunctionGroupId,
            dataGroupIds);

        logger.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
            internalServiceAgreementId, externalUserId, adminFunctionGroupId, dataGroupIds);
    }

    public UserContext getUserContextBasedOnMSAByExternalUserId(String externalUserId) {
        this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalUserId = this.userPresentationRestClient.getUserByExternalId(externalUserId)
            .getId();

        LegalEntityByUserGetResponseBody legalEntity = this.userPresentationRestClient
            .retrieveLegalEntityByExternalUserId(externalUserId);

        String internalServiceAgreementId = this.legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .getId();

        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();

        return new UserContext()
            .withInternalUserId(internalUserId)
            .withExternalUserId(externalUserId)
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
