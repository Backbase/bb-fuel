package com.backbase.testing.dataloader.setup;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CONVERSATIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.UserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.testing.dataloader.configurators.ContactsConfigurator;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.MessagesConfigurator;
import com.backbase.testing.dataloader.configurators.PaymentsConfigurator;
import com.backbase.testing.dataloader.configurators.PermissionsConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.testing.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.testing.dataloader.configurators.TransactionsConfigurator;
import com.backbase.testing.dataloader.dto.ArrangementId;
import com.backbase.testing.dataloader.dto.CurrencyDataGroup;
import com.backbase.testing.dataloader.dto.LegalEntityWithUsers;
import com.backbase.testing.dataloader.dto.UserContext;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LegalEntitiesWithUsersAssembler {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private ContactsConfigurator contactsConfigurator = new ContactsConfigurator();
    private PaymentsConfigurator paymentsConfigurator = new PaymentsConfigurator();
    private MessagesConfigurator messagesConfigurator = new MessagesConfigurator();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    private PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();
    private ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    private ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient = new ServiceAgreementsPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();
    private TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();
    private LegalEntityWithUsers[] entities = ParserUtil
        .convertJsonToObject(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON_LOCATION), LegalEntityWithUsers[].class);

    public LegalEntitiesWithUsersAssembler() throws IOException {
    }

    public void assembleUsersWithAndWithoutFunctionDataGroupsPrivileges() throws IOException {
        final LegalEntityWithUsers[] entitiesWithoutPermissions = ParserUtil
            .convertJsonToObject(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON_LOCATION),
                LegalEntityWithUsers[].class);
        Arrays.stream(this.entities).forEach(entity -> {
            this.legalEntitiesAndUsersConfigurator
                .ingestUsersUnderComposedLegalEntity(entity.getUserExternalIds(), entity.getParentLegalEntityExternalId(), entity.getLegalEntityExternalId(),
                    entity.getLegalEntityName(), entity.getLegalEntityType());
            assembleUsersPrivilegesAndDataGroups(entity.getUserExternalIds());
        });
        Arrays.stream(entitiesWithoutPermissions).forEach(entity -> this.legalEntitiesAndUsersConfigurator
            .ingestUsersUnderComposedLegalEntity(entity.getUserExternalIds(), entity.getParentLegalEntityExternalId(), entity.getLegalEntityExternalId(),
                entity.getLegalEntityName(), entity.getLegalEntityType()));
    }

    public void assembleContactsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CONTACTS)) {
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.contactsConfigurator.ingestContacts();
            Arrays.stream(this.entities).map(LegalEntityWithUsers::getUserExternalIds).flatMap(List::stream).collect(Collectors.toList())
                .forEach(userId -> this.loginRestClient.login(userId, userId));
        }
    }

    public void assemblePaymentsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_PAYMENTS)) {
            Arrays.stream(this.entities).map(LegalEntityWithUsers::getUserExternalIds).flatMap(List::stream).collect(Collectors.toList())
                .forEach(userId -> this.paymentsConfigurator.ingestPaymentOrders(userId));
        }
    }

    public void assembleConversationsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CONVERSATIONS)) {
            this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            Arrays.stream(this.entities).map(LegalEntityWithUsers::getUserExternalIds).flatMap(List::stream).collect(Collectors.toList())
                .forEach(userId -> this.messagesConfigurator.ingestConversations(userId));
        }
    }

    protected void assembleUsersPrivilegesAndDataGroups(List<String> userExternalIds) {
        Multimap<String, UserContext> legalEntitiesUserContextMap = ArrayListMultimap.create();
        this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        userExternalIds.forEach(id -> {
            final String legalEntityInternalId = this.userPresentationRestClient.retrieveLegalEntityByExternalUserId(id).then().statusCode(SC_OK)
                .extract()
                .as(LegalEntityByUserGetResponseBody.class).getId();
            if (!legalEntitiesUserContextMap.containsKey(legalEntityInternalId)) {
                this.serviceAgreementsConfigurator.updateMasterServiceAgreementWithExternalIdByLegalEntity(legalEntityInternalId);
            }
            UserContext userContext = getUserContextByExternalId(id);
            legalEntitiesUserContextMap.put(userContext.getInternalLegalEntityId(), userContext);
        });
        legalEntitiesUserContextMap.values()
            .forEach(userContext -> assembleFunctionGroupsAndAssignPermissions(userContext.getExternalUserId(), userContext.getInternalServiceAgreementId(),
                userContext.getExternalServiceAgreementId(),
                getDataGroupArrangementsForServiceAgreement(userContext.getExternalServiceAgreementId(), userContext.getExternalLegalEntityId()), true));
    }

    protected CurrencyDataGroup getDataGroupArrangementsForServiceAgreement(String externalServiceAgreementId, String externalLegalEntityId) {
        final CurrencyDataGroup group = new CurrencyDataGroup();
        List<Callable<Void>> taskList = new ArrayList<>();
        taskList.add(() -> generateTask(externalServiceAgreementId,
            () -> this.productSummaryConfigurator.ingestRandomCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId),
            group::withInternalRandomCurrencyDataGroupId));
        taskList
            .add(() -> generateTask(externalServiceAgreementId, () -> this.productSummaryConfigurator
                    .ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency.EUR),
                group::withInternalEurCurrencyDataGroupId));
        taskList
            .add(() -> generateTask(externalServiceAgreementId, () -> this.productSummaryConfigurator
                    .ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency.USD),
                group::withInternalUsdCurrencyDataGroupId));
        taskList.parallelStream().forEach(voidCallable -> {
            try {
                voidCallable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return group;
    }

    private void assembleFunctionGroupsAndAssignPermissions(String externalUserId, String internalServiceAgreementId, String externalServiceAgreementId,
        CurrencyDataGroup currencyDataGroup, boolean masterServiceAgreement) {
        FunctionsGetResponseBody[] functions = this.accessGroupIntegrationRestClient.retrieveFunctions()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionsGetResponseBody[].class);
        this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        Arrays.stream(functions).map(FunctionsGetResponseBody::getName).forEach(functionName -> {
            if (masterServiceAgreement) {
                this.permissionsConfigurator.assignPermissions(externalUserId, internalServiceAgreementId, functionName, this.accessGroupsConfigurator
                        .setupFunctionGroupWithAllPrivilegesByFunctionName(internalServiceAgreementId, externalServiceAgreementId, functionName),
                    currencyDataGroup);
                return;
            }
            this.permissionsConfigurator.assignPermissions(externalUserId, internalServiceAgreementId, functionName,
                this.accessGroupsConfigurator.ingestFunctionGroupWithAllPrivilegesByFunctionName(externalServiceAgreementId, functionName), currencyDataGroup);
        });
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
        List<ArrangementId> ids = new ArrayList<>();
        ids.addAll(supplier.get());
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
