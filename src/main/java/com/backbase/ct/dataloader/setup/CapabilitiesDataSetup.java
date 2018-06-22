package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_ACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_MESSAGES;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON;
import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.configurators.ActionsConfigurator;
import com.backbase.ct.dataloader.configurators.ContactsConfigurator;
import com.backbase.ct.dataloader.configurators.MessagesConfigurator;
import com.backbase.ct.dataloader.configurators.NotificationsConfigurator;
import com.backbase.ct.dataloader.configurators.PaymentsConfigurator;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.ct.dataloader.utils.ParserUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CapabilitiesDataSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private NotificationsConfigurator notificationsConfigurator = new NotificationsConfigurator();
    private ContactsConfigurator contactsConfigurator = new ContactsConfigurator();
    private PaymentsConfigurator paymentsConfigurator = new PaymentsConfigurator();
    private MessagesConfigurator messagesConfigurator = new MessagesConfigurator();
    private ActionsConfigurator actionsConfigurator = new ActionsConfigurator();
    private LegalEntityWithUsers[] entities = ParserUtil
        .convertJsonToObject(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON),
            LegalEntityWithUsers[].class);

    public CapabilitiesDataSetup() throws IOException {
    }

    public void ingestBankNotifications() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.notificationsConfigurator.ingestNotifications();
        }
    }

    public void ingestContactsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CONTACTS)) {
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(userId -> this.contactsConfigurator.ingestContacts(userId));
        }
    }

    public void ingestPaymentsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_PAYMENTS)) {
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(userId -> this.paymentsConfigurator.ingestPaymentOrders(userId));
        }
    }

    public void ingestConversationsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_MESSAGES)) {
            this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(userId -> this.messagesConfigurator.ingestConversations(userId));
        }
    }

    public void ingestActionsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACTIONS)) {
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(userId -> this.actionsConfigurator.ingestActions(userId));
        }
    }
}
