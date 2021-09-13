package com.backbase.ct.bbfuel.setup;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACCOUNTSTATEMENTS_USERS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_ACCOUNT_STATEMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_ACTIONS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_BATCHES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_CONTACTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_NOTIFICATIONS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_BILLPAY;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_BILLPAY_ACCOUNTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_CONTACTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_LIMITS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_MESSAGES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_NOTIFICATIONS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_PAYMENTS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_POCKETS;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_INGEST_POSITIVE_PAY_CHECKS;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.pfm.PocketTailorActuatorClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.configurator.AccountStatementsConfigurator;
import com.backbase.ct.bbfuel.configurator.ActionsConfigurator;
import com.backbase.ct.bbfuel.configurator.ApprovalsConfigurator;
import com.backbase.ct.bbfuel.configurator.BillPayConfigurator;
import com.backbase.ct.bbfuel.configurator.ContactsConfigurator;
import com.backbase.ct.bbfuel.configurator.LimitsConfigurator;
import com.backbase.ct.bbfuel.configurator.MessagesConfigurator;
import com.backbase.ct.bbfuel.configurator.NotificationsConfigurator;
import com.backbase.ct.bbfuel.configurator.PaymentsConfigurator;
import com.backbase.ct.bbfuel.configurator.PocketsConfigurator;
import com.backbase.ct.bbfuel.configurator.PositivePayConfigurator;
import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.UserContext;
import com.backbase.ct.bbfuel.service.LegalEntityService;
import com.backbase.ct.bbfuel.service.UserContextService;
import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapabilitiesDataSetup extends BaseSetup {

    private final UserContextService userContextService;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final AccessControlSetup accessControlSetup;
    private final ApprovalsConfigurator approvalsConfigurator;
    private final LimitsConfigurator limitsConfigurator;
    private final NotificationsConfigurator notificationsConfigurator;
    private final ContactsConfigurator contactsConfigurator;
    private final PaymentsConfigurator paymentsConfigurator;
    private final MessagesConfigurator messagesConfigurator;
    private final ActionsConfigurator actionsConfigurator;
    private final BillPayConfigurator billpayConfigurator;
    private final PocketsConfigurator pocketsConfigurator;
    private final LegalEntityService legalEntityService;
    private final AccountStatementsConfigurator accountStatementsConfigurator;
    private final PositivePayConfigurator positivePayConfigurator;
    private final UserPresentationRestClient userPresentationRestClient;
    private final PocketTailorActuatorClient pocketTailorActuatorClient;

    /**
     * Ingest data with services of projects APPR, PO, LIM, NOT, CON, MC, ACT, BPAY and Pockets.
     */
    @Override
    public void initiate() {
        log.debug("initiate CapabilitiesDataSetup");
        this.ingestApprovals();
        this.ingestPaymentsPerUser();
        this.ingestLimits();
        this.ingestBankNotifications();
        this.ingestContactsPerUser();
        this.ingestConversationsPerUser();
        this.ingestActionsPerUser();
        this.ingestBillPayUsers();
        this.ingestPockets();
        this.ingestAccountStatementForSelectedUser();
        this.ingestPositivePayChecksForSelectedUser();
    }

    private void ingestApprovals() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS)
            || this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_CONTACTS)
            || this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_NOTIFICATIONS)
            || this.globalProperties.getBoolean(PROPERTY_INGEST_APPROVALS_FOR_BATCHES)) {
            this.loginRestClient.loginBankAdmin();
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            this.approvalsConfigurator.setupApprovalTypesAndPolicies();

            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().forEach(legalEntityWithUsers -> {
                List<User> users = legalEntityWithUsers.getUsers();
                UserContext userContext = getRandomUserContextBasedOnMsaByExternalUserId(users);

                this.approvalsConfigurator.setupAccessControlAndPerformApprovalAssignments(
                    userContext.getExternalServiceAgreementId(),
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
        LegalEntityWithUsers fallbackLegalEntityWithAdminUser = new LegalEntityWithUsers();
        fallbackLegalEntityWithAdminUser
            .setUsers(singletonList(User.builder()
                .externalId(legalEntityService.getRootAdmin())
                .build()));
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_NOTIFICATIONS)) {
            List<User> users = this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport()
                .stream()
                .findFirst()
                .orElse(fallbackLegalEntityWithAdminUser).getUsers();
            UserContext userContext = getRandomUserContextBasedOnMsaByExternalUserId(users);
            this.notificationsConfigurator.ingestNotifications(
                userContext.getExternalUserId()
            );
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
            this.loginRestClient.loginBankAdmin();
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().forEach(legalEntityWithUsers ->
                this.messagesConfigurator.ingestTopics()
            );
        }
    }

    private void ingestActionsPerUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACTIONS)) {
            List<String> externalUserIds = this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport()
                .stream()
                .filter(legalEntities -> legalEntities.getUsers()
                    .stream()
                    // TODO: Check job profile of user for "create" permission on "Manage Action Recipes"
                    .noneMatch(user -> "employee".equals(user.getRole())))
                .collect(Collectors.toList())
                .stream()
                .map(LegalEntityWithUsers::getUsers)
                .flatMap(List::stream)
                .map(User::getExternalId)
                .collect(Collectors.toList());

            externalUserIds.forEach(this.actionsConfigurator::ingestActions);
        }
    }

    private void ingestBillPayUsers() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_BILLPAY)) {
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupport().forEach((le) -> {
                billpayConfigurator.ingestBillPayUserAndAccounts(le,
                    this.globalProperties.getBoolean(PROPERTY_INGEST_BILLPAY_ACCOUNTS));
            });
        }
    }

    private void ingestPockets() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_POCKETS)) {

            this.loginRestClient.loginBankAdmin();
            this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            // only use legal entities with category 'retail', users with productGroupName 'Retail Pocket'
            List<User> retailUsers = this.accessControlSetup
                .getLegalEntitiesWithUsersExcludingSupport()
                .stream()
                .filter(legalEntityWithUsers -> legalEntityWithUsers.getCategory().isRetail())
                .map(LegalEntityWithUsers::getUsers)
                .flatMap(Collection::stream)
                .filter(user -> user.getExternalId().equalsIgnoreCase("user"))
                .filter(user -> user.getRole().equalsIgnoreCase("retail"))
                .filter(user -> StringUtils.isNotEmpty(user.getProductGroupNames()
                        .stream()
                        .filter(productGroupName -> productGroupName.equals("Retail Pocket"))
                        .findFirst()
                        .get()))
                .collect(Collectors.toList());

            retailUsers.forEach(retailUser -> {
                log.debug("Going to ingest pockets for retail user {}", retailUser);

                LegalEntityBase legalEntity = this.userPresentationRestClient
                    .retrieveLegalEntityByExternalUserId(retailUser.getExternalId());

                String parentPocketArrangementId = pocketsConfigurator.ingestPocketParentArrangementAndSetEntitlements(
                    legalEntity);
                if (parentPocketArrangementId != null) {
                    pocketTailorActuatorClient.createArrangedLegalEntity(parentPocketArrangementId, legalEntity);
                }

                this.loginRestClient.login(retailUser.getExternalId(), retailUser.getExternalId());
                userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
                this.pocketsConfigurator.ingestPockets(legalEntity.getExternalId());
            });
        }
    }

    private void ingestAccountStatementForSelectedUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_ACCOUNT_STATEMENTS)) {
            String externalUserIds = this.globalProperties.getString(PROPERTY_ACCOUNTSTATEMENTS_USERS);
            Splitter.on(';').trimResults().split(externalUserIds).forEach(externalUserId -> {
                this.accountStatementsConfigurator.ingestAccountStatements(externalUserId);
            });
         }
     }

    private void ingestPositivePayChecksForSelectedUser() {
        if (this.globalProperties.getBoolean(PROPERTY_INGEST_POSITIVE_PAY_CHECKS)) {
            this.accessControlSetup.getLegalEntitiesWithUsersExcludingSupportAndEmployee().stream()
                    .map(LegalEntityWithUsers::getUserExternalIds)
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
                    .forEach(this.positivePayConfigurator::ingestPositivePayChecks);
        }
    }
}

