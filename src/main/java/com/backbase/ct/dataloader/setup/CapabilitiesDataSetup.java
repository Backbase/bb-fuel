package com.backbase.ct.dataloader.setup;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_ACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_CONTACTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_LIMITS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_MESSAGES;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON;

import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.configurator.ActionsConfigurator;
import com.backbase.ct.dataloader.configurator.ApprovalsConfigurator;
import com.backbase.ct.dataloader.configurator.ContactsConfigurator;
import com.backbase.ct.dataloader.configurator.LimitsConfigurator;
import com.backbase.ct.dataloader.configurator.MessagesConfigurator;
import com.backbase.ct.dataloader.configurator.NotificationsConfigurator;
import com.backbase.ct.dataloader.configurator.PaymentsConfigurator;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.UserContext;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.ct.dataloader.util.ParserUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final AccessControlSetup accessControlSetup;
    private final ApprovalsConfigurator approvalsConfigurator;
    private final LimitsConfigurator limitsConfigurator;
    private final NotificationsConfigurator notificationsConfigurator;
    private final ContactsConfigurator contactsConfigurator;
    private final PaymentsConfigurator paymentsConfigurator;
    private final MessagesConfigurator messagesConfigurator;
    private final ActionsConfigurator actionsConfigurator;
    private final Random random;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
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

    public void ingestApprovals() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS) ||
            this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS)) {
            this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            this.approvalsConfigurator.setupApprovalTypesAndPolicies();

            Arrays.stream(this.entities)
                .forEach(legalEntityWithUsers -> {
                    List<String> externalUserIds = legalEntityWithUsers.getUserExternalIds();

                    UserContext userContext = accessControlSetup
                        .getUserContextBasedOnMSAByExternalUserId(
                            externalUserIds.get(random.nextInt(externalUserIds.size())));

                    this.approvalsConfigurator.setupAccessControlAndPerformApprovalAssignments(
                        userContext.getExternalServiceAgreementId(),
                        userContext.getExternalLegalEntityId(),
                        externalUserIds);
                });
        }
    }

    public void ingestLimits() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_LIMITS)) {
            Arrays.stream(this.entities)
                .forEach(legalEntityWithUsers -> {
                    List<String> externalUserIds = legalEntityWithUsers.getUserExternalIds();

                    UserContext userContext = accessControlSetup
                        .getUserContextBasedOnMSAByExternalUserId(
                            externalUserIds.get(random.nextInt(externalUserIds.size())));

                    this.limitsConfigurator.ingestLimits(userContext.getInternalServiceAgreementId());
                });
        }
    }

    public void ingestBankNotifications() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.notificationsConfigurator.ingestNotifications();
        }
    }

    public void ingestContactsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CONTACTS)) {
            Arrays.stream(this.entities)
                .forEach(legalEntityWithUsers -> {
                    List<String> externalUserIds = legalEntityWithUsers.getUserExternalIds();

                    UserContext userContext = accessControlSetup
                        .getUserContextBasedOnMSAByExternalUserId(
                            externalUserIds.get(random.nextInt(externalUserIds.size())));

                    this.contactsConfigurator.ingestContacts(
                        userContext.getExternalServiceAgreementId(),
                        userContext.getExternalUserId());
                });
        }
    }

    public void ingestPaymentsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_PAYMENTS)) {
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(this.paymentsConfigurator::ingestPaymentOrders);
        }
    }

    public void ingestConversationsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_MESSAGES)) {
            this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(this.messagesConfigurator::ingestConversations);
        }
    }

    public void ingestActionsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACTIONS)) {
            Arrays.stream(this.entities)
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(this.actionsConfigurator::ingestActions);
        }
    }
}
