package com.backbase.testing.dataloader.setup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.UserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.testing.dataloader.configurators.ContactsConfigurator;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.MessagesConfigurator;
import com.backbase.testing.dataloader.configurators.PaymentsConfigurator;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_CONVERSATIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_ENTITLEMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INGEST_TRANSACTIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_USERS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_USERS_WITHOUT_PERMISSIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static com.backbase.testing.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.testing.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static org.apache.http.HttpStatus.SC_OK;

public class UsersSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
    private ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
    private ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient = new ServiceAgreementsPresentationRestClient();
    private TransactionsConfigurator transactionsConfigurator = new TransactionsConfigurator();
    private LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
    private ContactsConfigurator contactsConfigurator = new ContactsConfigurator();
    private PaymentsConfigurator paymentsConfigurator = new PaymentsConfigurator();
    private MessagesConfigurator messagesConfigurator = new MessagesConfigurator();
    private UserList[] userLists = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_USERS_JSON_LOCATION), UserList[].class);

    public UsersSetup() throws IOException {
    }

    public void setupUsersWithAndWithoutFunctionDataGroupsPrivileges() throws IOException {
        if (globalProperties.getBoolean(PROPERTY_INGEST_ENTITLEMENTS)) {
            UserList[] usersWithoutPermissionsLists = ParserUtil.convertJsonToObject(globalProperties.getString(PROPERTY_USERS_WITHOUT_PERMISSIONS), UserList[].class);

            Arrays.stream(userLists).forEach(userList -> {
                List<String> externalUserIds = userList.getExternalUserIds();

                legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);

                for (String externalUserId : externalUserIds) {
                    serviceAgreementsConfigurator.updateMasterServiceAgreementWithExternalIdByUser(externalUserId);
                }

                setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(externalUserIds);
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
                    accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
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
            accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            Arrays.stream(userLists).forEach(userList -> {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> messagesConfigurator.ingestConversations(externalUserId));
            });
        }
    }

    public void setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(List<String> externalUserIds) {
        List<UserContext> userContextList = new ArrayList<>();
        Map<String, String> serviceAgreementLegalEntityIds = new HashMap<>();
        Map<String, CurrencyDataGroup> serviceAgreementDataGroupIds = new HashMap<>();

        for (String externalUserId : externalUserIds) {
            UserContext userContext = getUserContextBasedOnMasterServiceAgreement(externalUserId);

            userContextList.add(userContext);

            serviceAgreementLegalEntityIds.put(userContext.getExternalServiceAgreementId(), userContext.getExternalLegalEntityId());
        }

        for (Map.Entry<String, String> entry : serviceAgreementLegalEntityIds.entrySet()) {
            String externalServiceAgreementId = entry.getKey();
            String externalLegalEntityId =  entry.getValue();

            CurrencyDataGroup currencyDataGroup = setupArrangementsPerDataGroupForLegalEntity(externalServiceAgreementId, externalLegalEntityId);

            serviceAgreementDataGroupIds.put(externalServiceAgreementId, currencyDataGroup);
        }

        for (UserContext userContext : userContextList) {
            setupFunctionDataGroupAndPrivilegesUnderLegalEntity(userContext, serviceAgreementDataGroupIds.get(userContext.getExternalServiceAgreementId()));
        }
    }

    private void setupFunctionDataGroupAndPrivilegesUnderLegalEntity(UserContext userContext, CurrencyDataGroup currencyDataGroup) {
        FunctionsGetResponseBody[] functions = accessGroupIntegrationRestClient.retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class);

        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        Arrays.stream(functions).forEach(function -> {
            String functionName = function.getName();

            switch (functionName) {
                case SEPA_CT_FUNCTION_NAME:
                    accessGroupsConfigurator.setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndServiceAgreement(userContext, functionName, Collections.singletonList(currencyDataGroup.getInternalEurCurrencyDataGroupId()));
                    break;
                case US_DOMESTIC_WIRE_FUNCTION_NAME:
                case US_FOREIGN_WIRE_FUNCTION_NAME:
                    accessGroupsConfigurator.setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndServiceAgreement(userContext, functionName, Collections.singletonList(currencyDataGroup.getInternalUsdCurrencyDataGroupId()));
                    break;
                default:
                    accessGroupsConfigurator.setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndServiceAgreement(userContext, functionName, Arrays.asList(currencyDataGroup.getInternalRandomCurrencyDataGroupId(), currencyDataGroup.getInternalEurCurrencyDataGroupId(), currencyDataGroup.getInternalUsdCurrencyDataGroupId()));
            }
        });
    }

    private CurrencyDataGroup setupArrangementsPerDataGroupForLegalEntity(String externalServiceAgreementId, String externalLegalEntityId) {
        List<ArrangementId> randomCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator.ingestRandomCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId));
        List<ArrangementId> eurCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator.ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency.EUR));
        List<ArrangementId> usdCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator.ingestSpecificCurrencyArrangementsByLegalEntityAndReturnArrangementIds(externalLegalEntityId, ArrangementsPostRequestBodyParent.Currency.USD));

        String randomCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(externalServiceAgreementId, randomCurrencyArrangementIds);
        String eurCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(externalServiceAgreementId, eurCurrencyArrangementIds);
        String usdCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(externalServiceAgreementId, usdCurrencyArrangementIds);

        if (globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            List<ArrangementId> arrangementIds = new ArrayList<>();

            arrangementIds.addAll(randomCurrencyArrangementIds);
            arrangementIds.addAll(eurCurrencyArrangementIds);
            arrangementIds.addAll(usdCurrencyArrangementIds);

            arrangementIds.parallelStream().forEach(arrangementId -> transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId()));
        }

        return new CurrencyDataGroup()
                .withInternalRandomCurrencyDataGroupId(randomCurrencyDataGroupId)
                .withInternalEurCurrencyDataGroupId(eurCurrencyDataGroupId)
                .withInternalUsdCurrencyDataGroupId(usdCurrencyDataGroupId);
    }

    private UserContext getUserContextBasedOnMasterServiceAgreement(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);

        UserContext userContext = accessGroupPresentationRestClient.getUserContextBasedOnMasterServiceAgreement();

        String internalUserId = userPresentationRestClient.getUserByExternalId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(UserGetResponseBody.class)
            .getId();

        String externalLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByUserGetResponseBody.class)
            .getExternalId();

        String externalServiceAgreementId = serviceAgreementsPresentationRestClient.retrieveServiceAgreement(userContext.getInternalServiceAgreementId())
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getExternalId();

        return userContext
            .withInternalUserId(internalUserId)
            .withExternalUserId(externalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withExternalLegalEntityId(externalLegalEntityId);
    }
}
