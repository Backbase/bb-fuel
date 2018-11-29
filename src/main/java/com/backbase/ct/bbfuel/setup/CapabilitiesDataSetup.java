package com.backbase.ct.bbfuel.setup;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_ACTIONS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_CONTACTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_LIMITS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_MESSAGES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.configurator.ActionsConfigurator;
import com.backbase.ct.bbfuel.configurator.ApprovalsConfigurator;
import com.backbase.ct.bbfuel.configurator.ContactsConfigurator;
import com.backbase.ct.bbfuel.configurator.LimitsConfigurator;
import com.backbase.ct.bbfuel.configurator.MessagesConfigurator;
import com.backbase.ct.bbfuel.configurator.NotificationsConfigurator;
import com.backbase.ct.bbfuel.configurator.PaymentsConfigurator;
import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.UserContext;
import com.backbase.ct.bbfuel.dto.entitlement.JobProfile;
import com.backbase.ct.bbfuel.service.LegalEntityWithUsersService;
import com.backbase.ct.bbfuel.service.UserContextService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapabilitiesDataSetup extends BaseSetup {

    private final UserContextService userContextService;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final AccessControlSetup accessControlSetup;
    private final ApprovalsConfigurator approvalsConfigurator;
    private final LimitsConfigurator limitsConfigurator;
    private final NotificationsConfigurator notificationsConfigurator;
    private final ContactsConfigurator contactsConfigurator;
    private final PaymentsConfigurator paymentsConfigurator;
    private final MessagesConfigurator messagesConfigurator;
    private final ActionsConfigurator actionsConfigurator;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    /**
     * Ingest data with services of projects APPR, PO, LIM, NOT, CON, MC and ACT.
     */
    @Override
    public void initiate() {
        this.ingestApprovals();
        this.ingestPaymentsPerUser();
        this.ingestLimits();
        this.ingestBankNotifications();
        this.ingestContactsPerUser();
        this.ingestConversationsPerUser();
        this.ingestActionsPerUser();
    }

    private void ingestApprovals() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS)
            || this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS)) {
            this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            this.approvalsConfigurator.setupApprovalTypesAndPolicies();

            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().forEach(legalEntityWithUsers -> {
                List<User> users = legalEntityWithUsers.getUsers();
                UserContext userContext = getRandomUserContextBasedOnMsaByExternalUserId(users);

                this.approvalsConfigurator.setupAccessControlAndPerformApprovalAssignments(
                    userContext.getExternalServiceAgreementId(),
                    userContext.getExternalLegalEntityId(),
                    users.size());
            });
        }
    }

    private UserContext getRandomUserContextBasedOnMsaByExternalUserId(List<User> users) {
        return userContextService
            .getUserContextBasedOnMSAByExternalUserId(
                getRandomFromList(users));
    }

    private void ingestLimits() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_LIMITS)) {
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().forEach(legalEntityWithUsers -> {
                List<User> users = legalEntityWithUsers.getUsers();
                UserContext userContext = getRandomUserContextBasedOnMsaByExternalUserId(users);

                this.limitsConfigurator.ingestLimits(userContext.getInternalServiceAgreementId());
            });
        }
    }

    private void ingestBankNotifications() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.notificationsConfigurator.ingestNotifications();
        }
    }

    private void ingestContactsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_CONTACTS)) {
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().forEach(legalEntityWithUsers -> {
                List<User> users = legalEntityWithUsers.getUsers();
                UserContext userContext = getRandomUserContextBasedOnMsaByExternalUserId(users);

                this.contactsConfigurator.ingestContacts(
                    userContext.getExternalServiceAgreementId(),
                    userContext.getExternalUserId());
            });
        }
    }

    private void ingestPaymentsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_PAYMENTS)) {
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().stream()
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(this.paymentsConfigurator::ingestPaymentOrders);
        }
    }

    private void ingestConversationsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_MESSAGES)) {
            this.loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().stream()
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList())
                .forEach(this.messagesConfigurator::ingestConversations);
        }
    }

    private void ingestActionsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACTIONS)) {
            List<String> externalUserIds = this.accessControlSetup
                .getLegalEntitiesWithUsersNotHavingAnyOf(Arrays.asList(
                    JobProfile.JOB_PROFILE_NAME_SUPPORT, JobProfile.JOB_PROFILE_NAME_FINANCE_EMPLOYEE))
                .stream()
                .map(LegalEntityWithUsers::getUserExternalIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());

            externalUserIds.forEach(this.actionsConfigurator::ingestActions);
        }
    }
}
