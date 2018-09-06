package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_ACCESS_CONTROL;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_BALANCE_HISTORY;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.client.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.configurator.AccessGroupsConfigurator;
import com.backbase.ct.dataloader.configurator.LegalEntitiesAndUsersConfigurator;
import com.backbase.ct.dataloader.configurator.ProductSummaryConfigurator;
import com.backbase.ct.dataloader.configurator.ServiceAgreementsConfigurator;
import com.backbase.ct.dataloader.configurator.TransactionsConfigurator;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.dto.CurrencyDataGroup;
import com.backbase.ct.dataloader.dto.LegalEntityContext;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.UserContext;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.ct.dataloader.util.ParserUtil;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessControlSetup {

    private final static Logger LOGGER = LoggerFactory.getLogger(CapabilitiesDataSetup.class);

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final LoginRestClient loginRestClient;
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
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
    // TODO refactor to have it parsed once (duplicated in CapabilitiesDataSetup)
    private LegalEntityWithUsers[] entities = initialiseLegalEntityWithUsers();

    public LegalEntityWithUsers[] initialiseLegalEntityWithUsers() {
        LegalEntityWithUsers[] entities;
        try {
            entities = ParserUtil.convertJsonToObject(this.globalProperties.getString(
                PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON),
                LegalEntityWithUsers[].class);

        } catch (IOException e) {
            LOGGER.error("Failed parsing file with entities", e);
            throw new RuntimeException(e.getMessage());
        }
        return entities;
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(rootEntitlementsAdmin);
            this.productSummaryConfigurator.ingestProducts();
            assembleFunctionDataGroupsAndPermissions(singletonList(rootEntitlementsAdmin));
        }
    }

    public void setupAccessControlForUsers() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            Arrays.stream(this.entities)
                .forEach(entity -> {
                    this.legalEntitiesAndUsersConfigurator.ingestUsersUnderLegalEntity(
                        entity.getUserExternalIds(),
                        entity.getParentLegalEntityExternalId(),
                        entity.getLegalEntityExternalId(),
                        entity.getLegalEntityName(),
                        entity.getLegalEntityType());

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
            final String legalEntityInternalId = this.userPresentationRestClient
                .retrieveLegalEntityByExternalUserId(userExternalId)
                .getId();

            if (!legalEntitiesUserContextMap.containsKey(legalEntityInternalId)) {
                this.serviceAgreementsConfigurator
                    .updateMasterServiceAgreementWithExternalIdByLegalEntity(legalEntityInternalId);
            }

            UserContext userContext = getUserContextBasedOnMSAByExternalUserId(userExternalId);
            legalEntitiesUserContextMap.put(userContext.getInternalLegalEntityId(), userContext);

            if (legalEntityContext.getCurrencyDataGroup() == null) {
                legalEntityContext.setCurrencyDataGroup(
                    ingestDataGroupArrangementsForServiceAgreement(userContext.getExternalServiceAgreementId(),
                        userContext.getExternalLegalEntityId()));
            }

        });

        legalEntitiesUserContextMap.values()
            .forEach(userContext -> ingestFunctionGroupsAndAssignPermissions(userContext.getExternalUserId(),
                userContext.getInternalServiceAgreementId(),
                userContext.getExternalServiceAgreementId(),
                legalEntityContext.getCurrencyDataGroup()));
    }

    protected CurrencyDataGroup ingestDataGroupArrangementsForServiceAgreement(String externalServiceAgreementId,
        String externalLegalEntityId) {
        final CurrencyDataGroup currencyDataGroup = new CurrencyDataGroup();
        List<Callable<Void>> taskList = new ArrayList<>();

        taskList.add(() -> generateTask(externalServiceAgreementId, "International",
            () -> this.productSummaryConfigurator.ingestArrangementsByLegalEntity(externalLegalEntityId, null),
            currencyDataGroup::withInternationalDataGroupId));

        taskList.add(() -> generateTask(externalServiceAgreementId, "Europe",
            () -> this.productSummaryConfigurator.ingestArrangementsByLegalEntity(externalLegalEntityId, Currency.EUR),
            currencyDataGroup::withEuropeDataGroupId));

        taskList.add(() -> generateTask(externalServiceAgreementId, "United States",
            () -> this.productSummaryConfigurator.ingestArrangementsByLegalEntity(externalLegalEntityId, Currency.USD),
            currencyDataGroup::withUsDataGroupId));

        taskList.parallelStream()
            .forEach(voidCallable -> {
                try {
                    voidCallable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        return currencyDataGroup;
    }

    private void ingestFunctionGroupsAndAssignPermissions(String externalUserId, String internalServiceAgreementId,
        String externalServiceAgreementId,
        CurrencyDataGroup currencyDataGroup) {

        String adminFunctionGroupId = this.accessGroupsConfigurator.ingestAdminFunctionGroup(externalServiceAgreementId);

        List<String> dataGroupIds = asList(
            currencyDataGroup.getEuropeDataGroupId(),
            currencyDataGroup.getUsDataGroupId(),
            currencyDataGroup.getInternationalDataGroupId());

        this.accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            adminFunctionGroupId,
            dataGroupIds);

        LOGGER.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
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

    private Void generateTask(String externalServiceAgreementId, String dataGroupName, Supplier<List<ArrangementId>> supplier,
        Consumer<String> consumer) {
        List<ArrangementId> ids = new ArrayList<>(supplier.get());
        String currencyDataGroupId = this.accessGroupsConfigurator
            .ingestDataGroupForArrangements(externalServiceAgreementId, dataGroupName, ids);

        if (this.globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            ids.parallelStream()
                .forEach(arrangementId -> this.transactionsConfigurator
                    .ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
        }

        if (this.globalProperties.getBoolean(PROPERTY_INGEST_BALANCE_HISTORY)) {
            ids.parallelStream()
                .forEach(arrangementId -> this.productSummaryConfigurator
                    .ingestBalanceHistory(arrangementId.getExternalArrangementId()));
        }

        consumer.accept(currencyDataGroupId);
        return null;
    }
}
