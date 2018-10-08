package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.ArrangementType.FINANCE_INTERNATIONAL;
import static com.backbase.ct.bbfuel.data.ArrangementType.GENERAL_BUSINESS;
import static com.backbase.ct.bbfuel.data.ArrangementType.GENERAL_RETAIL;
import static com.backbase.ct.bbfuel.data.ArrangementType.INTERNATIONAL_TRADE;
import static com.backbase.ct.bbfuel.data.ArrangementType.PAYROLL;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.BHD;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.CNY;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.EUR;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.INR;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.JPY;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.TRY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CommonConstants {

    private CommonConstants() {
    }

    // Switch flags
    public static final String PROPERTY_CONFIGURATION_SWITCHER = "use.local.configurations";

    // Environment
    public static final String PROPERTY_LOGIN_PATH = "login.path";
    public static final String PROPERTIES_FILE_NAME = "data.properties";
    public static final String ENVIRONMENT_PROPERTIES_FILE_NAME = "environment.properties";
    public static final String LOCAL_PROPERTIES_FILE_NAME = "local.properties";
    public static final String PROPERTY_INFRA_BASE_URI = "infra.base.uri";
    public static final String PROPERTY_GATEWAY_PATH = "gateway.path";
    public static final String PROPERTY_ACCESS_CONTROL_BASE_URI = "access.control.base.uri";
    public static final String PROPERTY_PRODUCT_SUMMARY_BASE_URI = "product.summary.base.uri";
    public static final String PROPERTY_TRANSACTIONS_BASE_URI = "transactions.base.uri";
    public static final String PROPERTY_CONTACT_MANAGER_BASE_URI = "contact.manager.base.uri";
    public static final String PROPERTY_APPROVALS_BASE_URI = "approvals.base.uri";
    public static final String PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES = "healthcheck.timeout.in.minutes";
    public static final String PROPERTY_LOG_ALL_REQUESTS_RESPONSES = "log.all.requests.responses";
    public static final String PROPERTY_MULTI_TENANCY_ENVIRONMENT = "multi.tenancy.environment";
    public static final String PROPERTY_TENANT_ID = "tenant.id";

    // Users
    public static final String PROPERTY_ROOT_ENTITLEMENTS_ADMIN = "root.entitlements.admin";
    public static final String PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON = "legal.entities.with.users.json";
    public static final String PROPERTY_SERVICE_AGREEMENTS_JSON = "service.agreements.json";

    // Entitlements
    static final String EXTERNAL_LEGAL_ENTITY_ID_PREFIX = "C00000";
    public static final String EXTERNAL_ROOT_LEGAL_ENTITY_ID = EXTERNAL_LEGAL_ENTITY_ID_PREFIX + "0";
    public static final String SEPA_CT_FUNCTION_NAME = "SEPA CT";
    public static final String US_DOMESTIC_WIRE_FUNCTION_NAME = "US Domestic Wire";
    public static final String US_FOREIGN_WIRE_FUNCTION_NAME = "US Foreign Wire";
    public static final String PAYMENTS_RESOURCE_NAME = "Payments";
    public static final String CONTACTS_RESOURCE_NAME = "Contacts";
    public static final String CONTACTS_FUNCTION_NAME = CONTACTS_RESOURCE_NAME;
    public static final String PRIVILEGE_CREATE = "create";
    public static final String PROPERTY_INGEST_ACCESS_CONTROL = "ingest.access.control";
    public static final String PROPERTY_INGEST_CUSTOM_SERVICE_AGREEMENTS = "ingest.custom.service.agreements";
    public static final String PROPERTY_JOB_PROFILES_JSON_LOCATION = "job.profiles.json";

    // User data
    static final String PROPERTY_DEBIT_CARDS_MIN = "debit.cards.min";
    static final String PROPERTY_DEBIT_CARDS_MAX = "debit.cards.max";
    public static final String PROPERTY_CONTACT_ACCOUNTS_MIN = "contact.accounts.min";
    public static final String PROPERTY_CONTACT_ACCOUNTS_MAX = "contact.accounts.max";
    public static final String PROPERTY_ARRANGEMENTS_GENERAL_MIN = "arrangements.general.min";
    public static final String PROPERTY_ARRANGEMENTS_GENERAL_MAX = "arrangements.general.max";
    public static final String PROPERTY_INGEST_INTERNATIONAL_AND_PAYROLL_DATA_GROUPS = "ingest.international.and.payroll.data.groups";
    public static final String PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MIN = "arrangements.international.trade.min";
    public static final String PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MAX = "arrangements.international.trade.max";
    public static final String PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MIN = "arrangements.finance.international.min";
    public static final String PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MAX = "arrangements.finance.international.max";
    public static final String PROPERTY_ARRANGEMENTS_PAYROLL_MIN = "arrangements.payroll.min";
    public static final String PROPERTY_ARRANGEMENTS_PAYROLL_MAX = "arrangements.payroll.max";
    public static final String PROPERTY_INGEST_BALANCE_HISTORY = "ingest.balance.history";
    public static final String PROPERTY_TRANSACTIONS_MIN = "transactions.min";
    public static final String PROPERTY_TRANSACTIONS_MAX = "transactions.max";
    public static final String PROPERTY_INGEST_TRANSACTIONS = "ingest.transactions";
    public static final String PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS = "use.pfm.categories.for.transactions";
    public static final String PROPERTY_NOTIFICATIONS_MIN = "notifications.min";
    public static final String PROPERTY_NOTIFICATIONS_MAX = "notifications.max";
    public static final String PROPERTY_INGEST_NOTIFICATIONS = "ingest.notifications";
    public static final String PROPERTY_INGEST_APPROVALS_FOR_PAYMENTS = "ingest.approvals.for.payments";
    public static final String PROPERTY_INGEST_APPROVALS_FOR_CONTACTS = "ingest.approvals.for.contacts";
    public static final String PROPERTY_INGEST_LIMITS = "ingest.limits";
    public static final String PROPERTY_CONTACTS_MIN = "contacts.min";
    public static final String PROPERTY_CONTACTS_MAX = "contacts.max";
    public static final String PROPERTY_INGEST_CONTACTS = "ingest.contacts";
    public static final String PROPERTY_PAYMENTS_MIN = "payments.min";
    public static final String PROPERTY_PAYMENTS_MAX = "payments.max";
    public static final String PROPERTY_INGEST_PAYMENTS = "ingest.payments";
    public static final String PROPERTY_MESSAGES_MIN = "messages.min";
    public static final String PROPERTY_MESSAGES_MAX = "messages.max";
    public static final String PROPERTY_INGEST_MESSAGES = "ingest.messages";
    public static final String PROPERTY_ACTIONS_MIN = "actions.min";
    public static final String PROPERTY_ACTIONS_MAX = "actions.max";
    public static final String PROPERTY_INGEST_ACTIONS = "ingest.actions";

    // Product summary
    static final String PROPERTY_PRODUCTS_JSON_LOCATION = "products.json";
    static final List<String> GENERAL_BUSINESS_CURRENT_ACCOUNT_NAMES = asList(
        "Factory",
        "GBF Corporate",
        "Legal",
        "Manufacturing",
        "Marketing",
        "Payables",
        "Personnel",
        "Receivables",
        "Sales",
        "Support"
    );

    static final List<String> FINANCE_INTERNATIONAL_CURRENT_ACCOUNT_NAMES = asList(
        "Assets",
        "Liability",
        "Equity",
        "Revenue",
        "Expenses",
        "Executive expenses",
        "Transport"
    );

    static final Map<Currency, String> INTERNATIONAL_TRADE_CURRENCY_NAME_MAP = new EnumMap<>(Currency.class);

    static {
        INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.put(BHD, "Bahrain sales");
        INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.put(CNY, "China sales");
        INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.put(JPY, "Japan sales");
        INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.put(INR, "India sales");
        INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.put(TRY, "Turkey sales");
        INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.put(EUR, "Belgium sales");
    }

    static final Map<ArrangementType, List<String>> CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP =
        new EnumMap<>(ArrangementType.class);

    static {
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(GENERAL_RETAIL, singletonList("Current Account"));
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(GENERAL_BUSINESS, GENERAL_BUSINESS_CURRENT_ACCOUNT_NAMES);
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(INTERNATIONAL_TRADE,
            new ArrayList<>(INTERNATIONAL_TRADE_CURRENCY_NAME_MAP.values()));
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP
            .put(FINANCE_INTERNATIONAL, FINANCE_INTERNATIONAL_CURRENT_ACCOUNT_NAMES);
        CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.put(PAYROLL, singletonList("Payroll"));
    }

    // Payments
    public static final String PAYMENT_TYPE_SEPA_CREDIT_TRANSFER = "SEPA_CREDIT_TRANSFER";
    public static final String PAYMENT_TYPE_US_DOMESTIC_WIRE = "US_DOMESTIC_WIRE";

    // Transactions
    public static final List<String> TRANSACTION_TYPE_GROUPS = asList(
        "Payment",
        "Withdrawal",
        "Loans",
        "Fees"
    );

    public static final List<String> TRANSACTION_TYPES = asList(
        "SEPA CT",
        "SEPA DD",
        "BACS (UK)",
        "Faster payment (UK)",
        "CHAPS (UK)",
        "International payment",
        "Loan redemption",
        "Interest settlement"
    );
}
