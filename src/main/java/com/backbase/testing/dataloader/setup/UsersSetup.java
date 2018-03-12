package com.backbase.testing.dataloader.setup;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CONVERSATIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_USERS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_USERS_WITHOUT_PERMISSIONS;
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
import com.backbase.testing.dataloader.dto.UserContext;
import com.backbase.testing.dataloader.dto.UserList;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@Deprecated
public class UsersSetup {

    protected GlobalProperties globalProperties = GlobalProperties.getInstance();
    LoginRestClient loginRestClient = new LoginRestClient();
    UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    ContactsConfigurator contactsConfigurator = new ContactsConfigurator();
    PaymentsConfigurator paymentsConfigurator = new PaymentsConfigurator();
    MessagesConfigurator messagesConfigurator = new MessagesConfigurator();

    UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    PermissionsConfigurator permissionsConfigurator = new PermissionsConfigurator();
    ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient = new ServiceAgreementsPresentationRestClient();
    LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();
    TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();
    private UserList[] userLists = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_USERS_JSON_LOCATION), UserList[].class);

    public UsersSetup() throws IOException {
    }

    public void setupUsersWithAndWithoutFunctionDataGroupsPrivileges() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            UserList[] usersWithoutPermissionsLists = ParserUtil
                .convertJsonToObject(globalProperties.getString(PROPERTY_USERS_WITHOUT_PERMISSIONS), UserList[].class);

            Arrays.stream(userLists).parallel().forEach(userList -> {
                List<String> externalUserIds = userList.getExternalUserIds();

                legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);

                setupUsersWithAllFunctionDataGroupsAndPrivileges(externalUserIds);
            });

            Arrays.stream(usersWithoutPermissionsLists).parallel().forEach(usersWithoutPermissionsList -> {
                List<String> externalUserIds = usersWithoutPermissionsList.getExternalUserIds();

                legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
            });
        }
    }

    public void setupContactsPerUser() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_CONTACTS)) {
            Arrays.stream(userLists).forEach(userList -> {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> {
                    loginRestClient.login(externalUserId, externalUserId);
                    userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
                    contactsConfigurator.ingestContacts();
                });
            });
        }
    }

    public void setupPaymentsPerUser() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_PAYMENTS)) {
            Arrays.stream(userLists).forEach(userList -> {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> paymentsConfigurator.ingestPaymentOrders(externalUserId));
            });
        }
    }

    public void setupConversationsPerUser() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_CONVERSATIONS)) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            Arrays.stream(userLists).forEach(userList -> {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> messagesConfigurator.ingestConversations(externalUserId));
            });
        }
    }

    protected void setupUsersWithAllFunctionDataGroupsAndPrivileges(List<String> externalUserIds) {
        Set<String> internalLegalEntityIds = new HashSet<>();
        List<UserContext> userContextList = new ArrayList<>();
        Map<String, String> serviceAgreementLegalEntityIds = new HashMap<>();
        Map<String, CurrencyDataGroup> serviceAgreementDataGroupIds = new HashMap<>();

        for (String externalUserId : externalUserIds) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            internalLegalEntityIds.add(userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(LegalEntityByUserGetResponseBody.class)
                .getId());
        }

        internalLegalEntityIds
            .forEach(internalLegalEntityId -> serviceAgreementsConfigurator.updateMasterServiceAgreementWithExternalIdByLegalEntity(internalLegalEntityId));

        for (String externalUserId : externalUserIds) {
            UserContext userContext = getUserContextBasedOnMasterServiceAgreement(externalUserId);

            userContextList.add(userContext);

            serviceAgreementLegalEntityIds.put(userContext.getExternalServiceAgreementId(), userContext.getExternalLegalEntityId());
        }

        for (Map.Entry<String, String> entry : serviceAgreementLegalEntityIds.entrySet()) {
            String externalServiceAgreementId = entry.getKey();
            String externalLegalEntityId = entry.getValue();

            CurrencyDataGroup currencyDataGroup = setupArrangementsPerDataGroupForServiceAgreement(externalServiceAgreementId, externalLegalEntityId);

            serviceAgreementDataGroupIds.put(externalServiceAgreementId, currencyDataGroup);
        }

        for (UserContext userContext : userContextList) {
            setupFunctionGroupsAndAssignPermissions(userContext.getExternalUserId(), userContext.getInternalServiceAgreementId(),
                userContext.getExternalServiceAgreementId(), serviceAgreementDataGroupIds.get(userContext.getExternalServiceAgreementId()), true);
        }
    }

    protected void setupFunctionGroupsAndAssignPermissions(String externalUserId, String internalServiceAgreementId, String externalServiceAgreementId,
        CurrencyDataGroup currencyDataGroup, boolean masterServiceAgreement) {
        FunctionsGetResponseBody[] functions = accessGroupIntegrationRestClient.retrieveFunctions()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionsGetResponseBody[].class);

        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        Arrays.stream(functions).forEach(function -> {
            String functionName = function.getName();
            String functionGroupId;

            if (masterServiceAgreement) {
                functionGroupId = accessGroupsConfigurator
                    .setupFunctionGroupWithAllPrivilegesByFunctionName(internalServiceAgreementId, externalServiceAgreementId, functionName);
            } else {
                functionGroupId = accessGroupsConfigurator.ingestFunctionGroupWithAllPrivilegesByFunctionName(externalServiceAgreementId, functionName);
            }

            permissionsConfigurator.assignPermissions(externalUserId, internalServiceAgreementId, functionName, functionGroupId, currencyDataGroup);
        });
    }

    protected CurrencyDataGroup setupArrangementsPerDataGroupForServiceAgreement(String externalServiceAgreementId, String externalLegalEntityId) {
        final CurrencyDataGroup group = new CurrencyDataGroup();
        List<Callable<Void>> taskList = new ArrayList<>();

        taskList.add(() -> {
            List<ArrangementId> randomCurrencyArrangementIds = new ArrayList<>(
                productSummaryConfigurator.ingestRandomCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId));
            String randomCurrencyDataGroupId = accessGroupsConfigurator
                .ingestDataGroupForArrangements(externalServiceAgreementId, randomCurrencyArrangementIds);
            if (globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
                randomCurrencyArrangementIds.parallelStream()
                    .forEach(arrangementId -> transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
            }
            group.withInternalRandomCurrencyDataGroupId(randomCurrencyDataGroupId);
            return null;
        });

        taskList.add(() -> {
            List<ArrangementId> eurCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator
                .ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency.EUR));
            String eurCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(externalServiceAgreementId, eurCurrencyArrangementIds);
            if (globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
                eurCurrencyArrangementIds.parallelStream()
                    .forEach(arrangementId -> transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
            }
            group.withInternalEurCurrencyDataGroupId(eurCurrencyDataGroupId);
            return null;
        });

        taskList.add(() -> {
            List<ArrangementId> usdCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator
                .ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency.USD));
            String usdCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(externalServiceAgreementId, usdCurrencyArrangementIds);
            if (globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
                usdCurrencyArrangementIds.parallelStream()
                    .forEach(arrangementId -> transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
            }
            group.withInternalUsdCurrencyDataGroupId(usdCurrencyDataGroupId);
            return null;
        });

        taskList.parallelStream().forEach(voidCallable -> {
            try {
                voidCallable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return group;
    }

    protected UserContext getUserContextBasedOnMasterServiceAgreement(String externalUserId) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalUserId = userPresentationRestClient.getUserByExternalId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(UserGetResponseBody.class)
            .getId();

        LegalEntityByUserGetResponseBody legalEntity = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByUserGetResponseBody.class);

        String internalServiceAgreementId = legalEntityPresentationRestClient.getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getId();

        String externalServiceAgreementId = serviceAgreementsPresentationRestClient.retrieveServiceAgreement(internalServiceAgreementId)
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
}
