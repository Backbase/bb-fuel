package com.backbase.ct.bbfuel.data;

public final class CommonConstants {

    private CommonConstants() {
    }

    // Environment
    public static final String PROPERTIES_FILE_NAME = "data.properties";
    public static final String CUSTOM_PROPERTIES_PATH = "custom.properties.path";
    public static final String ADDITIONAL_PROPERTIES_PATH = "additional.properties.path";
    public static final String ENVIRONMENT_PROPERTIES_FILE_NAME = "environment.properties";
    public static final String PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES = "healthcheck.timeout.in.minutes";
    public static final String PROPERTY_HEALTH_CHECK_USE_ACTUATOR = "healthcheck.use.actuator";
    public static final String PROPERTY_LOG_ALL_REQUESTS_RESPONSES = "log.all.requests.responses";
    public static final String PROPERTY_MULTI_TENANCY_ENVIRONMENT = "multi.tenancy.environment";
    public static final String PROPERTY_TENANT_ID = "tenant.id";

    // Users
    public static final String PROPERTY_ROOT_ENTITLEMENTS_ADMIN = "root.entitlements.admin";
    public static final String PROPERTY_ROOT_ENTITLEMENTS_ADMIN_PASSWORD = "root.entitlements.admin.password";
    public static final String PROPERTY_M10Y_LEGAL_ENTITIES_WITH_USERS_JSON =
        "multi.tenancy.legal.entities.with.users.json";
    public static final String PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON = "legal.entities.with.users.json";
    public static final String PROPERTY_SERVICE_AGREEMENTS_JSON = "service.agreements.json";

    // Entitlements
    static final String EXTERNAL_LEGAL_ENTITY_ID_PREFIX = "C00000";
    public static final String EXTERNAL_ROOT_LEGAL_ENTITY_ID = EXTERNAL_LEGAL_ENTITY_ID_PREFIX + "0";
    public static final String PRODUCT_SUMMARY_FUNCTION_NAME = "Product Summary";
    public static final String SEPA_CT_FUNCTION_NAME = "SEPA CT";
    public static final String SEPA_CT_INTRACOMPANY_FUNCTION_NAME = "SEPA CT - Intracompany";
    public static final String ACH_DEBIT_FUNCTION_NAME = "ACH Debit";
    public static final String ACH_DEBIT_INTRACOMPANY_FUNCTION_NAME = "ACH Debit - Intracompany";
    public static final String US_DOMESTIC_WIRE_FUNCTION_NAME = "US Domestic Wire";
    public static final String US_DOMESTIC_WIRE_INTRACOMPANY_FUNCTION_NAME = "US Domestic Wire - Intracompany";
    public static final String US_FOREIGN_WIRE_FUNCTION_NAME = "US Foreign Wire";
    public static final String US_FOREIGN_WIRE_INTRACOMPANY_FUNCTION_NAME = "US Foreign Wire - Intracompany";
    public static final String PRODUCT_SUMMARY_RESOURCE_NAME = "Product Summary";
    public static final String PAYMENTS_RESOURCE_NAME = "Payments";
    public static final String CONTACTS_RESOURCE_NAME = "Contacts";
    public static final String NOTIFICATIONS_RESOURCE_NAME = "Notifications";
    public static final String CONTACTS_FUNCTION_NAME = CONTACTS_RESOURCE_NAME;
    public static final String NOTIFICATIONS_FUNCTION_NAME = "Manage Notifications";
    public static final String BATCH_RESOURCE_NAME = "Batch";
    public static final String BATCH_SEPA_CT_FUNCTION_NAME = "Batch - SEPA CT";
    public static final String PRIVILEGE_CREATE = "create";
    public static final String PRIVILEGE_VIEW = "view";
    public static final String PROPERTY_INGEST_ACCESS_CONTROL = "ingest.access.control";
    public static final String PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS = "ingest.custom.service.agreements";
    public static final String PROPERTY_JOB_PROFILES_JSON_LOCATION = "job.profiles.json";
    public static final String PROPERTY_PRODUCT_GROUP_SEED_JSON_LOCATION = "product.group.seed.json";
    public static final String PROPERTY_ADDITIONAL_PRODUCT_GROUP_SEED_JSON_LOCATION = "additional.product.group.seed.json";

    // User data
    public static final String PROPERTY_CONTACT_ACCOUNTS_MIN = "contact.accounts.min";
    public static final String PROPERTY_CONTACT_ACCOUNTS_MAX = "contact.accounts.max";
    public static final String PROPERTY_INGEST_BALANCE_HISTORY = "ingest.balance.history";
    public static final String PROPERTY_TRANSACTIONS_MIN = "transactions.min";
    public static final String PROPERTY_TRANSACTIONS_MAX = "transactions.max";
    public static final String PROPERTY_INGEST_TRANSACTIONS = "ingest.transactions";
    public static final String PROPERTY_NOTIFICATIONS_MIN = "notifications.min";
    public static final String PROPERTY_NOTIFICATIONS_MAX = "notifications.max";
    public static final String PROPERTY_INGEST_NOTIFICATIONS = "ingest.notifications";
    public static final String PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS = "ingest.approvals.for.payments";
    public static final String PROPERTY_INGEST_APPROVALS_FOR_CONTACTS = "ingest.approvals.for.contacts";
    public static final String PROPERTY_INGEST_APPROVALS_FOR_NOTIFICATIONS = "ingest.approvals.for.notifications";
    public static final String PROPERTY_INGEST_APPROVALS_FOR_BATCHES = "ingest.approvals.for.batches";
    public static final String PROPERTY_INGEST_LIMITS = "ingest.limits";
    public static final String PROPERTY_INGEST_CONTENT_FOR_PAYMENTS = "ingest.content.for.payments";
    public static final String PROPERTY_CONTACTS_MIN = "contacts.min";
    public static final String PROPERTY_CONTACTS_MAX = "contacts.max";
    public static final String PROPERTY_CONTACTS_ACCOUNT_TYPES = "contacts.account.types";
    public static final String PROPERTY_INGEST_CONTACTS = "ingest.contacts";
    public static final String PROPERTY_PAYMENTS_MIN = "payments.min";
    public static final String PROPERTY_PAYMENTS_MAX = "payments.max";
    public static final String PROPERTY_INGEST_PAYMENTS = "ingest.payments";
    public static final String PROPERTY_MESSAGE_TOPICS_MIN = "topics.message.min";
    public static final String PROPERTY_MESSAGE_TOPICS_MAX = "topics.message.max";
    public static final String PROPERTY_INGEST_MESSAGES = "ingest.messages";
    public static final String PROPERTY_ACTIONS_MIN = "actions.min";
    public static final String PROPERTY_ACTIONS_MAX = "actions.max";
    public static final String PROPERTY_INGEST_ACTIONS = "ingest.actions";
    public static final String PROPERTY_INGEST_BILLPAY = "ingest.billpay";
    public static final String PROPERTY_INGEST_BILLPAY_ACCOUNTS = "ingest.billpay.accounts";
    public static final String PROPERTY_INGEST_ACCOUNT_STATEMENTS = "ingest.accountStatements";
    public static final String PROPERTY_ACCOUNTSTATEMENTS_MIN = "accountStatement.min";
    public static final String PROPERTY_ACCOUNTSTATEMENTS_MAX = "accountStatement.max";
    public static final String PROPERTY_ACCOUNTSTATEMENTS_USERS = "accountStatement.externalUserIds";
    public static final String PROPERTY_INGEST_POSITIVE_PAY_CHECKS = "ingest.positivePay";
    public static final String PROPERTY_POSITIVEPAY_MIN = "positivePay.min";
    public static final String PROPERTY_POSITIVEPAY_MAX = "positivePay.max";
    public static final String PROPERTY_INGEST_POCKETS = "ingest.pockets";
    public static final String PROPERTY_POCKET_MAPPING_MODE = "pocket.mapping.mode";

    // Product summary
    public static final String PROPERTY_PRODUCTS_JSON_LOCATION = "products.json";
    public static final String PROPERTY_ARRANGEMENT_CURRENT_ACCOUNT_EXTERNAL_IDS = "arrangement.currentaccount.externalIds";
    public static final String PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_EXTERNAL_IDS = "arrangement.not.currentaccount.externalIds";
    public static final String PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_LEGAL_ENTITY_EXTERNAL_ID_LIMIT =
        "arrangement.not.currentaccount.legal.entity.externalId.limit";
    public static final String PROPERTY_ARRANGEMENT_NOT_CURRENT_ACCOUNT_PRODUCT_ID_LIMIT =
        "arrangement.not.currentaccount.productId.limit";

    // Payments
    public static final String PAYMENT_TYPE_SEPA_CREDIT_TRANSFER = "SEPA_CREDIT_TRANSFER";
    public static final String PAYMENT_TYPE_US_DOMESTIC_WIRE = "US_DOMESTIC_WIRE";
    public static final String PAYMENT_TYPE_ACH_DEBIT = "ACH_DEBIT";
    public static final String PAYMENT_TYPE_US_FOREIGN_WIRE = "US_FOREIGN_WIRE";
    public static final String PROPERTY_PAYMENTS_OOTB_TYPES = "payments.ootb.types";

    // Contacts
    public static final String IBAN_ACCOUNT_TYPE = "IBAN";
    public static final String BBAN_ACCOUNT_TYPE = "BBAN";

    // Transactions
    public static final String PROPERTY_TRANSACTIONS_DATA_JSON = "transactions.data.json";
    public static final String PROPERTY_POCKET_TRANSACTIONS_DATA_JSON = "pocket.transactions.data.json";
    public static final String PROPERTY_CURRENTACCOUNT_TRANSACTIONS_DATA_JSON = "currentaccount.transactions.data.json";
    public static final String PROPERTY_TRANSACTIONS_CHECK_IMAGES_DATA_JSON = "transactions-check-images.data.json";
    public static final String PROPERTY_TRANSACTIONS_CURRENCY = "transactions.currency";

    // Identity
    public static final String PROPERTY_IDENTITY_FEATURE_TOGGLE = "identity.feature.toggle";
    public static final String PROPERTY_IDENTITY_REALM = "identity.realm";
    public static final String PROPERTY_IDENTITY_CLIENT = "identity.client";
    public static final String IDENTITY_AUTH = "/auth/realms";
    public static final String IDENTITY_TOKEN_PATH = "/protocol/openid-connect/token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String SESSION_TOKEN = "session_state";

    // Pockets
    public static final String PROPERTY_POCKETS_DATA_JSON = "pocket.data.json";
}
