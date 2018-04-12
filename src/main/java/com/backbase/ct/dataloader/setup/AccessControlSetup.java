package com.backbase.ct.dataloader.setup;

import com.backbase.ct.dataloader.clients.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.ct.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.ct.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.ct.dataloader.configurators.PermissionsConfigurator;
import com.backbase.ct.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.ct.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.ct.dataloader.configurators.TransactionsConfigurator;
import com.backbase.ct.dataloader.dto.ArrangementId;
import com.backbase.ct.dataloader.dto.CurrencyDataGroup;
import com.backbase.ct.dataloader.dto.LegalEntityContext;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.UserContext;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.ct.dataloader.utils.ParserUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.UserGetResponseBody;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON_LOCATION;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON_LOCATION;
import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessControlSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    private PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();
    private ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    private ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient = new ServiceAgreementsPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();
    private TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();
    private LegalEntityWithUsers[] entities = ParserUtil
        .convertJsonToObject(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON_LOCATION), LegalEntityWithUsers[].class);

    public AccessControlSetup() throws IOException {
    }

    public void setupBankWithEntitlementsAdminAndProducts() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            this.legalEntitiesAndUsersConfigurator.ingestRootLegalEntityAndEntitlementsAdmin(USER_ADMIN);
            this.productSummaryConfigurator.ingestProducts();
            assembleFunctionDataGroupsAndPermissions(singletonList(USER_ADMIN));
        }
    }

    public void setupAccessControlForUsers() throws IOException {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            final LegalEntityWithUsers[] entitiesWithoutPermissions = ParserUtil.convertJsonToObject(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON_LOCATION), LegalEntityWithUsers[].class);

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
            final String legalEntityInternalId = this.userPresentationRestClient.retrieveLegalEntityByExternalUserId(userExternalId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(LegalEntityByUserGetResponseBody.class)
                .getId();

            if (!legalEntitiesUserContextMap.containsKey(legalEntityInternalId)) {
                this.serviceAgreementsConfigurator.updateMasterServiceAgreementWithExternalIdByLegalEntity(legalEntityInternalId);
            }

            UserContext userContext = getUserContextByExternalId(userExternalId);
            legalEntitiesUserContextMap.put(userContext.getInternalLegalEntityId(), userContext);

            if (legalEntityContext.getCurrencyDataGroup() == null) {
                legalEntityContext.setCurrencyDataGroup(ingestDataGroupArrangementsForServiceAgreement(userContext.getExternalServiceAgreementId(),
                    userContext.getExternalLegalEntityId()));
            }

        });

        legalEntitiesUserContextMap.values()
            .forEach(userContext -> ingestFunctionGroupsAndAssignPermissions(userContext.getExternalUserId(), userContext.getInternalServiceAgreementId(),
                userContext.getExternalServiceAgreementId(),
                legalEntityContext.getCurrencyDataGroup()));
    }

    protected CurrencyDataGroup ingestDataGroupArrangementsForServiceAgreement(String externalServiceAgreementId, String externalLegalEntityId) {
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

    private void ingestFunctionGroupsAndAssignPermissions(String externalUserId, String internalServiceAgreementId, String externalServiceAgreementId,
                                                          CurrencyDataGroup currencyDataGroup) {
        String sepaFunctionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            singletonList(SEPA_CT_FUNCTION_NAME));

        String usWireFunctionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesByFunctionNames(
            externalServiceAgreementId,
            asList(US_DOMESTIC_WIRE_FUNCTION_NAME, US_FOREIGN_WIRE_FUNCTION_NAME));

        String noSepaAndUsWireFunctionGroupId = this.accessGroupsConfigurator.ingestFunctionGroupsWithAllPrivilegesNotContainingProvidedFunctionNames(
            externalServiceAgreementId,
            asList(
                SEPA_CT_FUNCTION_NAME,
                US_DOMESTIC_WIRE_FUNCTION_NAME,
                US_FOREIGN_WIRE_FUNCTION_NAME));

        this.permissionsConfigurator.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            sepaFunctionGroupId,
            singletonList(currencyDataGroup.getInternalEurCurrencyDataGroupId()));

        this.permissionsConfigurator.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            usWireFunctionGroupId,
            singletonList(currencyDataGroup.getInternalUsdCurrencyDataGroupId()));

        this.permissionsConfigurator.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            noSepaAndUsWireFunctionGroupId,
            asList(
                currencyDataGroup.getInternalRandomCurrencyDataGroupId(),
                currencyDataGroup.getInternalEurCurrencyDataGroupId(),
                currencyDataGroup.getInternalUsdCurrencyDataGroupId()));
    }

    private UserContext getUserContextByExternalId(String externalUserId) {
        this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalUserId = this.userPresentationRestClient.getUserByExternalId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(UserGetResponseBody.class)
            .getId();

        LegalEntityByUserGetResponseBody legalEntity = this.userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByUserGetResponseBody.class);

        String internalServiceAgreementId = this.legalEntityPresentationRestClient.getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getId();

        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient.retrieveServiceAgreement(internalServiceAgreementId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getExternalId();

        return new UserContext()
            .withInternalUserId(internalUserId)
            .withExternalUserId(externalUserId)
            .withInternalServiceAgreementId(internalServiceAgreementId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withInternalLegalEntityId(legalEntity.getId())
            .withExternalLegalEntityId(legalEntity.getExternalId());
    }

    private Void generateTask(String externalServiceAgreementId, Supplier<List<ArrangementId>> supplier, Consumer<String> consumer) {
        List<ArrangementId> ids = new ArrayList<>(supplier.get());
        String currencyDataGroupId = this.accessGroupsConfigurator
            .ingestDataGroupForArrangements(externalServiceAgreementId, ids);
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            ids.parallelStream()
                .forEach(arrangementId -> this.transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
        }
        consumer.accept(currencyDataGroupId);
        return null;
    }
}
