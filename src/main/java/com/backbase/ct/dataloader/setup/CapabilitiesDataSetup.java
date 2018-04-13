package com.backbase.ct.dataloader.setup;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.configurators.ContactsConfigurator;
import com.backbase.ct.dataloader.configurators.MessagesConfigurator;
import com.backbase.ct.dataloader.configurators.NotificationsConfigurator;
import com.backbase.ct.dataloader.configurators.PaymentsConfigurator;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.ct.dataloader.utils.ParserUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.backbase.ct.dataloader.data.CommonConstants.*;

public class CapabilitiesDataSetup {

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private NotificationsConfigurator notificationsConfigurator = new NotificationsConfigurator();
    private ContactsConfigurator contactsConfigurator = new ContactsConfigurator();
    private PaymentsConfigurator paymentsConfigurator = new PaymentsConfigurator();
    private MessagesConfigurator messagesConfigurator = new MessagesConfigurator();
    private LegalEntityWithUsers[] entities = ParserUtil
        .convertJsonToObject(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON_LOCATION), LegalEntityWithUsers[].class);

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
                .forEach(userId -> {
                    this.loginRestClient.login(userId, userId);
                    this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
                    this.contactsConfigurator.ingestContacts();
                });
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
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CONVERSATIONS)) {
            this.loginRestClient.login(USER_ADMIN, USER_ADMIN);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(userId -> this.messagesConfigurator.ingestConversations(userId));
        }
    }
}
