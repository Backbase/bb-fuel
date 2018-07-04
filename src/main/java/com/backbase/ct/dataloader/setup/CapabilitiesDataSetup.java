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
import com.backbase.presentation.accessgroup.listener.client.v2.accessgroups.users.PresentationAccessgroupUsersClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapabilitiesDataSetup {
    private final static Logger LOGGER = LoggerFactory.getLogger(CapabilitiesDataSetup.class);

    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final LoginRestClient loginRestClient;

    private final PresentationAccessgroupUsersClient presentationAccessgroupUsersClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final NotificationsConfigurator notificationsConfigurator;
    private final ContactsConfigurator contactsConfigurator;
    private final PaymentsConfigurator paymentsConfigurator;
    private final MessagesConfigurator messagesConfigurator;
    private final ActionsConfigurator actionsConfigurator;
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
