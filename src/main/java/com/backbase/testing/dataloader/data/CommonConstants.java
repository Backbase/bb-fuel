package com.backbase.testing.dataloader.data;

public class CommonConstants {

    // Environment
    public static final String PROPERTIES_FILE_NAME = "environment.properties";
    public static final String PROPERTY_INFRA_BASE_URI = "infra.base.uri";
    public static final String PROPERTY_GATEWAY_PATH = "gateway.path";
    public static final String PROPERTY_ENTITLEMENTS_BASE_URI = "entitlements.base.uri";
    public static final String PROPERTY_PRODUCTSUMMARY_BASE_URI = "productsummary.base.uri";
    public static final String PROPERTY_TRANSACTIONS_BASE_URI = "transactions.base.uri";


    // Users
    public static final String USER_ADMIN = "admin";
    public static final String USERS_JSON_EXTERNAL_USER_IDS_FIELD = "externalUserIds";
    public static final String PROPERTY_USERS_JSON_LOCATION = "users.json.location";
    public static final String PROPERTY_USERS_WITHOUT_PERMISSIONS = "users.without.permissions.json.location";
    public static final String PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION = "serviceagreements.json.location";

    // Entitlements
    public static final String EXTERNAL_LEGAL_ENTITY_ID_PREFIX = "C00000";
    public static final String EXTERNAL_ROOT_LEGAL_ENTITY_ID = EXTERNAL_LEGAL_ENTITY_ID_PREFIX + "0";

    // Products
    public static final String PROPERTY_PRODUCTS_JSON_LOCATION = "products.json.location";

    // User data
    public static final String PROPERTY_ARRANGEMENTS_MIN = "arrangements.min";
    public static final String PROPERTY_ARRANGEMENTS_MAX = "arrangements.max";
    public static final String PROPERTY_DEBIT_CARDS_MIN = "debit.cards.min";
    public static final String PROPERTY_DEBIT_CARDS_MAX = "debit.cards.max";
    public static final String PROPERTY_TRANSACTIONS_MIN = "transactions.min";
    public static final String PROPERTY_TRANSACTIONS_MAX = "transactions.max";
}
