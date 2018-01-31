package com.backbase.testing.dataloader.data;

public final class CommonConstants {

    private CommonConstants() {
    }

    // Environment
    public static final String PROPERTIES_FILE_NAME = "environment.properties";
    public static final String PROPERTY_INFRA_BASE_URI = "infra.base.uri";
    public static final String PROPERTY_GATEWAY_PATH = "gateway.path";
    public static final String PROPERTY_ENTITLEMENTS_BASE_URI = "entitlements.base.uri";
    public static final String PROPERTY_PRODUCTSUMMARY_BASE_URI = "productsummary.base.uri";
    public static final String PROPERTY_TRANSACTIONS_BASE_URI = "transactions.base.uri";
    public static final String PROPERTY_HEALTHCHECK_TIMEOUT_IN_MINUTES = "healthcheck.timeout.in.minutes";

    // Users
    public static final String USER_ADMIN = "admin";
    public static final String PROPERTY_USERS_JSON_LOCATION = "users.json.location";
    public static final String PROPERTY_USERS_WITHOUT_PERMISSIONS = "users.without.permissions.json.location";
    public static final String PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION = "serviceagreements.json.location";

    // Entitlements
    public static final String EXTERNAL_LEGAL_ENTITY_ID_PREFIX = "C00000";
    public static final String EXTERNAL_ROOT_LEGAL_ENTITY_ID = EXTERNAL_LEGAL_ENTITY_ID_PREFIX + "0";
    public static final String SEPA_CT_FUNCTION_NAME = "SEPA CT";
    public static final String US_DOMESTIC_WIRE_FUNCTION_NAME = "US Domestic Wire";
    public static final String US_DOMESTIC_FOREIGN_FUNCTION_NAME = "US Domestic Foreign";
    public static final String PROPERTY_INGEST_ENTITLEMENTS = "ingest.entitlements";

    // Products
    public static final String PROPERTY_PRODUCTS_JSON_LOCATION = "products.json.location";

    // User data
    public static final String PROPERTY_ARRANGEMENTS_MIN = "arrangements.min";
    public static final String PROPERTY_ARRANGEMENTS_MAX = "arrangements.max";
    public static final String PROPERTY_DEBIT_CARDS_MIN = "debit.cards.min";
    public static final String PROPERTY_DEBIT_CARDS_MAX = "debit.cards.max";
    public static final String PROPERTY_TRANSACTIONS_MIN = "transactions.min";
    public static final String PROPERTY_TRANSACTIONS_MAX = "transactions.max";
    public static final String PROPERTY_INGEST_TRANSACTIONS = "ingest.transactions";
    public static final String PROPERTY_NOTIFICATIONS_MIN = "notifications.min";
    public static final String PROPERTY_NOTIFICATIONS_MAX = "notifications.max";
    public static final String PROPERTY_INGEST_NOTIFICATIONS = "ingest.notifications";
    public static final String PROPERTY_CONTACTS_MIN = "contacts.min";
    public static final String PROPERTY_CONTACTS_MAX = "contacts.max";
    public static final String PROPERTY_CONTACT_ACCOUNTS_MIN = "contact.accounts.min";
    public static final String PROPERTY_CONTACT_ACCOUNTS_MAX = "contact.accounts.max";
    public static final String PROPERTY_INGEST_CONTACTS = "ingest.contacts";
    public static final String PROPERTY_PAYMENTS_MIN = "payments.min";
    public static final String PROPERTY_PAYMENTS_MAX = "payments.max";
    public static final String PROPERTY_INGEST_PAYMENTS = "ingest.payments";
    public static final String PROPERTY_CONVERSATIONS_MIN = "conversations.min";
    public static final String PROPERTY_CONVERSATIONS_MAX = "conversations.max";
    public static final String PROPERTY_INGEST_CONVERSATIONS = "ingest.conversations";
}
