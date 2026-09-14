package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.data.CommonConstants.ACH_DEBIT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_ACH_CREDIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_ACH_DEBIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_ACH_REVERSAL;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_INTRACOMPANY;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_SEPA_CT;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_SEPA_DD;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_SEPA_DD_REVERSAL;
import static com.backbase.ct.bbfuel.data.CommonConstants.BATCH_TEMPLATES;
import static com.backbase.ct.bbfuel.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.SEPA_CT_INTRACOMPANY_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_DOMESTIC_WIRE_INTRACOMPANY_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.FCY_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_CROSS_BORDER_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_FX_FOREIGN_WIRE_FUNCTION_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Keeper of relevant Payments business functions. Although batch functions are part of a different resource
 * they have a tight relation with Payments.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentsFunctionService {

    /**
     * Business function names within Payments resource.
     */
    public static final List<String> PAYMENTS_FUNCTIONS = unmodifiableList(asList(
        SEPA_CT_FUNCTION_NAME,
        SEPA_CT_INTRACOMPANY_FUNCTION_NAME,
        US_DOMESTIC_WIRE_FUNCTION_NAME,
        US_DOMESTIC_WIRE_INTRACOMPANY_FUNCTION_NAME,
        US_FOREIGN_WIRE_FUNCTION_NAME,
        US_CROSS_BORDER_WIRE_FUNCTION_NAME,
        US_FX_FOREIGN_WIRE_FUNCTION_NAME,
        FCY_WIRE_FUNCTION_NAME,
        ACH_DEBIT_FUNCTION_NAME));

    /**
     * Business function names within Batch resource.
     */
    public static final List<String> BATCH_FUNCTIONS = unmodifiableList(asList(
        BATCH_ACH_CREDIT,
        BATCH_ACH_DEBIT,
        BATCH_ACH_REVERSAL,
        BATCH_INTRACOMPANY,
        BATCH_SEPA_CT,
        BATCH_SEPA_DD,
        BATCH_SEPA_DD_REVERSAL,
        BATCH_TEMPLATES
        )
    );

    /**
     * Determines the currency from the business function name.
     * SEPA, US FX Foreign Wire, and FCY Wire use EUR; other payment functions use USD.
     *
     * @param functionName business function name
     * @return currency
     */
    public static String determineCurrencyForFunction(String functionName) {
        if (functionName.toUpperCase().contains("SEPA")) {
            return "EUR";
        }
        if (US_FX_FOREIGN_WIRE_FUNCTION_NAME.equals(functionName)
            || FCY_WIRE_FUNCTION_NAME.equals(functionName)) {
            return "EUR";
        }
        return "USD";
    }

}
