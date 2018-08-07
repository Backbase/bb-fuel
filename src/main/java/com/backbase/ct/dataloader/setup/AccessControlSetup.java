package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.CommonConstants.PRODUCT_SUMMARY_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_ACCESS_CONTROL;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_BALANCE_HISTORY;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON;
import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.TRANSACTIONS_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
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
import com.backbase.ct.dataloader.configurator.PermissionsConfigurator;
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
    private final PermissionsConfigurator permissionsConfigurator;
    private final ServiceAgreementsConfigurator serviceAgreementsConfigurator;
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final TransactionsConfigurator transactionsConfigurator;
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
            this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(USER_ADMIN);
            this.productSummaryConfigurator.ingestProducts();
            assembleFunctionDataGroupsAndPermissions(singletonList(USER_ADMIN));
        }
    }

    public void setupAccessControlForUsers() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCESS_CONTROL)) {
            final LegalEntityWithUsers[] entitiesWithoutPermissions = ParserUtil.convertJsonToObject(
                this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON),
                LegalEntityWithUsers[].class);

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

            Arrays.stream(entitiesWithoutPermissions)
                .forEach(entity -> this.legalEntitiesAndUsersConfigurator.ingestUsersUnderLegalEntity(
                    entity.getUserExternalIds(),
                    entity.getParentLegalEntityExternalId(),
                    entity.getLegalEntityExternalId(),
                    entity.getLegalEntityName(),
                    entity.getLegalEntityType()));
        }
    }

    private void assembleFunctionDataGroupsAndPermissions(List<String> userExternalIds) {
        Multimap<String, UserContext> legalEntitiesUserContextMap = ArrayListMultimap.create();
        final LegalEntityContext legalEntityContext = new LegalEntityContext();
        this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
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

        taskList.add(() -> generateTask(externalServiceAgreementId,
            () -> this.productSummaryConfigurator.ingestRandomCurrencyArrangementsByLegalEntity(externalLegalEntityId),
            currencyDataGroup::withInternalRandomCurrencyDataGroupId));

        taskList.add(() -> generateTask(externalServiceAgreementId, () -> this.productSummaryConfigurator
                .ingestSpecificCurrencyArrangementsByLegalEntity(externalLegalEntityId, Currency.EUR),
            currencyDataGroup::withInternalEurCurrencyDataGroupId));

        taskList.add(() -> generateTask(externalServiceAgreementId, () -> this.productSummaryConfigurator
                .ingestSpecificCurrencyArrangementsByLegalEntity(externalLegalEntityId, Currency.USD),
            currencyDataGroup::withInternalUsdCurrencyDataGroupId));

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
        String sepaFunctionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            asList(SEPA_CT_FUNCTION_NAME, PRODUCT_SUMMARY_FUNCTION_NAME, TRANSACTIONS_FUNCTION_NAME));

        String usWireFunctionGroupId = this.accessGroupsConfigurator
            .ingestFunctionGroupWithAllPrivilegesByFunctionNames(
                externalServiceAgreementId,
                asList(US_DOMESTIC_WIRE_FUNCTION_NAME, US_FOREIGN_WIRE_FUNCTION_NAME, PRODUCT_SUMMARY_FUNCTION_NAME, TRANSACTIONS_FUNCTION_NAME));

        String noSepaAndUsWireFunctionGroupId = this.accessGroupsConfigurator
            .ingestFunctionGroupWithAllPrivilegesNotContainingProvidedFunctionNames(
                externalServiceAgreementId,
                asList(
                    SEPA_CT_FUNCTION_NAME,
                    US_DOMESTIC_WIRE_FUNCTION_NAME,
                    US_FOREIGN_WIRE_FUNCTION_NAME));

        List<String> sepaDataGroupIds = singletonList(currencyDataGroup.getInternalEurCurrencyDataGroupId());
        List<String> usWireDataGroupIds = singletonList(currencyDataGroup.getInternalUsdCurrencyDataGroupId());
        List<String> randomCurrencyDataGroupIds = singletonList(currencyDataGroup.getInternalRandomCurrencyDataGroupId());

        this.accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            sepaFunctionGroupId,
            sepaDataGroupIds);

        LOGGER.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
            internalServiceAgreementId, externalUserId, sepaFunctionGroupId, sepaDataGroupIds);

        this.accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            usWireFunctionGroupId,
            usWireDataGroupIds);

        LOGGER.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
            internalServiceAgreementId, externalUserId, usWireFunctionGroupId, usWireDataGroupIds);

        this.accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            noSepaAndUsWireFunctionGroupId,
            randomCurrencyDataGroupIds);

        LOGGER.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
            internalServiceAgreementId, externalUserId, noSepaAndUsWireFunctionGroupId, randomCurrencyDataGroupIds);
    }

    public UserContext getUserContextBasedOnMSAByExternalUserId(String externalUserId) {
        this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
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

    private Void generateTask(String externalServiceAgreementId, Supplier<List<ArrangementId>> supplier,
        Consumer<String> consumer) {
        List<ArrangementId> ids = new ArrayList<>(supplier.get());
        String currencyDataGroupId = this.accessGroupsConfigurator
            .ingestDataGroupForArrangements(externalServiceAgreementId, ids);

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
