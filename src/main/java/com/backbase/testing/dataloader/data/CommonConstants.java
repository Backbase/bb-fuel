package com.backbase.testing.dataloader.data;

public final class CommonConstants {

    private CommonConstants() {
    }

    // Switch flags
    public static final String PROPERTY_CONFIGURATION_SWITCHER = "use.local.configurations";

    // Environment
    public static final String PROPERTY_LOGIN_PATH = "login.path";
    public static final String PROPERTIES_FILE_NAME = "environment.properties";
    public static final String PROPERTY_INFRA_BASE_URI = "infra.base.uri";
    public static final String PROPERTY_GATEWAY_PATH = "gateway.path";
    public static final String PROPERTY_ENTITLEMENTS_BASE_URI = "entitlements.base.uri";
    public static final String PROPERTY_PRODUCT_SUMMARY_BASE_URI = "productsummary.base.uri";
    public static final String PROPERTY_TRANSACTIONS_BASE_URI = "transactions.base.uri";
    public static final String PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES = "healthcheck.timeout.in.minutes";

    // Local default environment values
    public static final String LOCAL_LOGIN_PATH = "/authentication-ldap/login";
    public static final String LOCAL_INFRA_BASE_URI = "http://localhost:8080";
    public static final String LOCAL_GATEWAY_PATH = "/gateway/api";
    public static final String LOCAL_ENTITLEMENTS_BASE_URI = "http://localhost:8087";
    public static final String LOCAL_PRODUCT_SUMMARY_BASE_URI = "http://localhost:8083";
    public static final String LOCAL_TRANSACTIONS_BASE_URI = "http://localhost:8084";

    // Users
    public static final String USER_ADMIN = "admin";
    public static final String PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON_LOCATION = "legal.entities.with.users.json.location";
    public static final String PROPERTY_LEGAL_ENTITIES_WITH_USERS_WITHOUT_PERMISSION_JSON_LOCATION = "legal.entities.with.users.without.permissions.json.location";
    public static final String PROPERTY_SERVICE_AGREEMENTS_JSON_LOCATION = "serviceagreements.json.location";

    // Entitlements
    static final String EXTERNAL_LEGAL_ENTITY_ID_PREFIX = "C00000";
    public static final String EXTERNAL_ROOT_LEGAL_ENTITY_ID = EXTERNAL_LEGAL_ENTITY_ID_PREFIX + "0";
    public static final String SEPA_CT_FUNCTION_NAME = "SEPA CT";
    public static final String US_DOMESTIC_WIRE_FUNCTION_NAME = "US Domestic Wire";
    public static final String US_FOREIGN_WIRE_FUNCTION_NAME = "US Foreign Wire";
    public static final String PAYMENTS_RESOURCE_NAME = "Payments";
    public static final String PRIVILEGE_CREATE = "create";
    public static final String PROPERTY_INGEST_ENTITLEMENTS = "ingest.entitlements";
    public static final String PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS = "ingest.custom.service.agreements";

    // Products
    static final String PROPERTY_PRODUCTS_JSON_LOCATION = "products.json.location";

    // User data
    static final String PROPERTY_DEBIT_CARDS_MIN = "debit.cards.min";
    static final String PROPERTY_DEBIT_CARDS_MAX = "debit.cards.max";
    static final String PROPERTY_CONTACT_ACCOUNTS_MIN = "contact.accounts.min";
    static final String PROPERTY_CONTACT_ACCOUNTS_MAX = "contact.accounts.max";

    public static final String PROPERTY_ARRANGEMENTS_MIN = "arrangements.min";
    public static final String PROPERTY_ARRANGEMENTS_MAX = "arrangements.max";
    public static final String PROPERTY_TRANSACTIONS_MIN = "transactions.min";
    public static final String PROPERTY_TRANSACTIONS_MAX = "transactions.max";
    public static final String PROPERTY_INGEST_TRANSACTIONS = "ingest.transactions";
    public static final String PROPERTY_NOTIFICATIONS_MIN = "notifications.min";
    public static final String PROPERTY_NOTIFICATIONS_MAX = "notifications.max";
    public static final String PROPERTY_INGEST_NOTIFICATIONS = "ingest.notifications";
    public static final String PROPERTY_CONTACTS_MIN = "contacts.min";
    public static final String PROPERTY_CONTACTS_MAX = "contacts.max";
    public static final String PROPERTY_INGEST_CONTACTS = "ingest.contacts";
    public static final String PROPERTY_PAYMENTS_MIN = "payments.min";
    public static final String PROPERTY_PAYMENTS_MAX = "payments.max";
    public static final String PROPERTY_INGEST_PAYMENTS = "ingest.payments";
    public static final String PROPERTY_CONVERSATIONS_MIN = "conversations.min";
    public static final String PROPERTY_CONVERSATIONS_MAX = "conversations.max";
    public static final String PROPERTY_INGEST_CONVERSATIONS = "ingest.conversations";
}
