package com.backbase.testing.dataloader.setup;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.configurators.AccessGroupsConfigurator;
import com.backbase.testing.dataloader.configurators.ContactsConfigurator;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.MessagesConfigurator;
import com.backbase.testing.dataloader.configurators.PaymentsConfigurator;
import com.backbase.testing.dataloader.configurators.ProductSummaryConfigurator;
import com.backbase.testing.dataloader.configurators.TransactionsConfigurator;
import com.backbase.testing.dataloader.dto.ArrangementId;
import com.backbase.testing.dataloader.dto.UserList;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import static com.backbase.testing.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.testing.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static org.apache.http.HttpStatus.SC_OK;

public class UsersSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersSetup.class);

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ProductSummaryConfigurator productSummaryConfigurator = new ProductSummaryConfigurator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupsConfigurator accessGroupsConfigurator = new AccessGroupsConfigurator();
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

            for (UserList userList : userLists) {
                List<String> externalUserIds = userList.getExternalUserIds();

                legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
                setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(externalUserIds);
            }

            for (UserList usersWithoutPermissionsList : usersWithoutPermissionsLists) {
                List<String> externalUserIds = usersWithoutPermissionsList.getExternalUserIds();

                legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
            }
        }
    }

    public void setupContactsPerUser() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_CONTACTS)) {
            for (UserList userList : userLists) {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> {
                    loginRestClient.login(externalUserId, externalUserId);
                    accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
                    contactsConfigurator.ingestContacts();
                });
            }
        }
    }

    public void setupPaymentsPerUser() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_PAYMENTS)) {
            for (UserList userList : userLists) {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> paymentsConfigurator.ingestPaymentOrders(externalUserId));
            }
        }
    }

    public void setupConversationsPerUser() {
        if (globalProperties.getBoolean(PROPERTY_INGEST_CONVERSATIONS)) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            for (UserList userList : userLists) {
                List<String> externalUserIds = userList.getExternalUserIds();

                externalUserIds.forEach(externalUserId -> messagesConfigurator.ingestConversations(externalUserId));
            }
        }
    }

    private void setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(List<String> externalUserIds) {
        for (String externalUserId : externalUserIds) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            LegalEntityByUserGetResponseBody legalEntity = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class);

            setupFunctionDataGroupAndPrivilegesUnderLegalEntity(legalEntity, externalUserId);
        }
    }

    void setupFunctionDataGroupAndPrivilegesUnderLegalEntity(LegalEntityByUserGetResponseBody legalEntity, String externalUserId) {
        FunctionsGetResponseBody[] functions = accessGroupIntegrationRestClient.retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class);

        List<ArrangementId> randomCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator.ingestRandomCurrencyArrangementsByLegalEntityAndReturnArrangementIds(legalEntity.getExternalId()));
        List<ArrangementId> eurCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator.ingestEurCurrencyArrangementsByLegalEntityAndReturnArrangementIds(legalEntity.getExternalId()));
        List<ArrangementId> usdCurrencyArrangementIds = new ArrayList<>(productSummaryConfigurator.ingestUsdCurrencyArrangementsByLegalEntityAndReturnArrangementIds(legalEntity.getExternalId()));

        List<String> randomCurrencyInternalArrangementIds = randomCurrencyArrangementIds.stream().map(ArrangementId::getInternalArrangementId).collect(Collectors.toList());
        List<String> eurCurrencyInternalArrangementIds = eurCurrencyArrangementIds.stream().map(ArrangementId::getInternalArrangementId).collect(Collectors.toList());
        List<String> usdCurrencyInternalArrangementIds = usdCurrencyArrangementIds.stream().map(ArrangementId::getInternalArrangementId).collect(Collectors.toList());

        String randomCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(legalEntity.getExternalId(), randomCurrencyInternalArrangementIds);
        String eurCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(legalEntity.getExternalId(), eurCurrencyInternalArrangementIds);
        String usdCurrencyDataGroupId = accessGroupsConfigurator.ingestDataGroupForArrangements(legalEntity.getExternalId(), usdCurrencyInternalArrangementIds);

        for (FunctionsGetResponseBody function : functions) {
            String functionName = function.getName();

            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            switch (functionName) {
                case SEPA_CT_FUNCTION_NAME:
                    accessGroupsConfigurator.setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndMasterServiceAgreement(legalEntity, externalUserId, functionName, eurCurrencyDataGroupId);
                    break;
                case US_DOMESTIC_WIRE_FUNCTION_NAME:
                case US_FOREIGN_WIRE_FUNCTION_NAME:
                    accessGroupsConfigurator.setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndMasterServiceAgreement(legalEntity, externalUserId, functionName, usdCurrencyDataGroupId);
                    break;
                default:
                    accessGroupsConfigurator.setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndMasterServiceAgreement(legalEntity, externalUserId, functionName, randomCurrencyDataGroupId);
            }
        }

        if (globalProperties.getBoolean(PROPERTY_INGEST_TRANSACTIONS)) {
            List<ArrangementId> arrangementIds = new ArrayList<>();

            arrangementIds.addAll(randomCurrencyArrangementIds);
            arrangementIds.addAll(eurCurrencyArrangementIds);
            arrangementIds.addAll(usdCurrencyArrangementIds);

            for (ArrangementId arrangementId : arrangementIds) {
                transactionsConfigurator.ingestTransactionsByArrangement(arrangementId.getExternalArrangementId());
            }
        }
    }
}
