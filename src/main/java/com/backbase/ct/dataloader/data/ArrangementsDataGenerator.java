package com.backbase.ct.dataloader.data;

import static com.backbase.ct.dataloader.data.ArrangementType.FINANCE_INTERNATIONAL;
import static com.backbase.ct.dataloader.data.ArrangementType.GENERAL_BUSINESS;
import static com.backbase.ct.dataloader.data.ArrangementType.GENERAL_RETAIL;
import static com.backbase.ct.dataloader.data.ArrangementType.INTERNATIONAL_TRADE;
import static com.backbase.ct.dataloader.data.ArrangementType.PAYROLL;
import static com.backbase.ct.dataloader.data.CommonConstants.CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_GENERAL_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_PAYROLL_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ARRANGEMENTS_PAYROLL_MIN;
import static com.backbase.ct.dataloader.data.ProductSummaryDataGenerator.generateArrangementsPostRequestBody;
import static com.backbase.ct.dataloader.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.dataloader.util.CommonHelpers.getRandomFromList;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.BHD;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.CAD;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.CNY;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.EUR;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.GBP;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.INR;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.JPY;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.TRY;
import static com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency.USD;
import static java.util.Arrays.asList;

import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class ArrangementsDataGenerator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    static List<ArrangementsPostRequestBody> generateFinanceInternationalArrangements(
        String externalLegalEntityId, Currency currency) {
        int numberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_FINANCE_INTERNATIONAL_MAX));
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<Currency> financeInternationalCurrencyList = asList(EUR, GBP, USD, CAD);
        int numberOfLoanAndSavingsAccountsPerGroup = 2;
        int numberOfInvestmentsAccountsPerGroup = 4;
        int numberOfCurrentAccountArrangements = numberOfArrangements <=
            (numberOfLoanAndSavingsAccountsPerGroup + numberOfInvestmentsAccountsPerGroup)
            ? numberOfArrangements : numberOfArrangements - (numberOfLoanAndSavingsAccountsPerGroup
                + numberOfInvestmentsAccountsPerGroup);

        List<String> currentAccountNames = CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(FINANCE_INTERNATIONAL);

        IntStream.range(0, numberOfCurrentAccountArrangements).parallel().forEach(randomNumber -> {
            Currency financeInternationalCurrency = currency == null
                ? getRandomFromList(financeInternationalCurrencyList)
                : currency;
            int currentAccountNameIndex = randomNumber < currentAccountNames.size() ? randomNumber
                : ThreadLocalRandom.current().nextInt(currentAccountNames.size());

            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1,
                    financeInternationalCurrency, FINANCE_INTERNATIONAL,
                    currentAccountNames.get(currentAccountNameIndex)));
        });

        if (numberOfArrangements > (numberOfLoanAndSavingsAccountsPerGroup
            + numberOfInvestmentsAccountsPerGroup)) {
            IntStream.range(0, numberOfInvestmentsAccountsPerGroup).parallel()
                .forEach(randomNumber -> {
                    Currency financeInternationalCurrency = currency == null
                        ? getRandomFromList(financeInternationalCurrencyList)
                        : currency;

                    arrangementsPostRequestBodies.add(
                        generateArrangementsPostRequestBody(externalLegalEntityId,
                            6, financeInternationalCurrency,
                            FINANCE_INTERNATIONAL));
                });

            IntStream.range(0, numberOfLoanAndSavingsAccountsPerGroup).parallel().forEach(randomNumber -> {
                Currency financeInternationalCurrency = currency == null
                    ? getRandomFromList(financeInternationalCurrencyList)
                    : currency;

                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId,
                        2, financeInternationalCurrency,
                        FINANCE_INTERNATIONAL));

                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId,
                        4, financeInternationalCurrency,
                        FINANCE_INTERNATIONAL));
            });
        }
        return arrangementsPostRequestBodies;
    }

    static List<ArrangementsPostRequestBody> generatePayrollArrangements(String externalLegalEntityId,
        Currency currency) {
        int numberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_PAYROLL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_PAYROLL_MAX));

        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<Currency> payrollCurrencyList = asList(EUR, GBP, USD, CAD);

        List<String> currentAccountNames = CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(PAYROLL);

        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            Currency payrollCurrency = currency == null
                ? getRandomFromList(payrollCurrencyList)
                : currency;
            int currentAccountNameIndex = randomNumber < currentAccountNames.size() ? randomNumber
                : ThreadLocalRandom.current().nextInt(currentAccountNames.size());

            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, payrollCurrency,
                    PAYROLL, currentAccountNames.get(currentAccountNameIndex)));
        });

        return arrangementsPostRequestBodies;
    }

    static List<ArrangementsPostRequestBody> generateInternationalTradeArrangements(
        String externalLegalEntityId, Currency currency) {
        int numberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_INTERNATIONAL_TRADE_MAX));
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<Currency> internationalTradeCurrencyList = asList(BHD, CNY, JPY, EUR, INR, TRY, EUR);

        IntStream.range(0, numberOfArrangements).parallel().forEach(randomNumber -> {
            Currency internationalTradeCurrency = currency == null
                ? getRandomFromList(internationalTradeCurrencyList)
                : currency;

            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, internationalTradeCurrency,
                    INTERNATIONAL_TRADE, null));
        });

        return arrangementsPostRequestBodies;
    }

    static List<ArrangementsPostRequestBody> generateGeneralBusinessArrangements(String externalLegalEntityId,
        Currency currency) {
        int numberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MAX));
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<Currency> arrangementCurrencyList = asList(EUR, GBP, USD, CAD);

        int numberOfSavingsAccountsPerGroup = 1;
        int numberOfCurrentAccountArrangements =
            numberOfArrangements <= numberOfSavingsAccountsPerGroup
                ? numberOfArrangements : numberOfArrangements - numberOfSavingsAccountsPerGroup;

        List<String> currentAccountNames = CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(GENERAL_BUSINESS);

        IntStream.range(0, numberOfCurrentAccountArrangements).parallel().forEach(randomNumber -> {
            Currency arrangementCurrency = currency == null
                ? getRandomFromList(arrangementCurrencyList)
                : currency;
            int currentAccountNameIndex = randomNumber < currentAccountNames.size() ? randomNumber
                : ThreadLocalRandom.current().nextInt(currentAccountNames.size());

            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, arrangementCurrency,
                    GENERAL_BUSINESS, currentAccountNames.get(currentAccountNameIndex)));
        });

        if (numberOfArrangements > numberOfSavingsAccountsPerGroup) {
            IntStream.range(0, numberOfSavingsAccountsPerGroup).parallel().forEach(randomNumber -> {
                Currency arrangementCurrency = currency == null
                    ? getRandomFromList(arrangementCurrencyList)
                    : currency;

                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId, 2, arrangementCurrency,
                        GENERAL_BUSINESS));
            });
        }

        return arrangementsPostRequestBodies;
    }

    static List<ArrangementsPostRequestBody> generateGeneralRetailArrangements(String externalLegalEntityId,
        Currency currency) {
        int numberOfArrangements = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MIN),
            globalProperties.getInt(PROPERTY_ARRANGEMENTS_GENERAL_MAX));
        List<ArrangementsPostRequestBody> arrangementsPostRequestBodies = new ArrayList<>();
        List<Currency> arrangementCurrencyList = asList(EUR, GBP, USD, CAD);

        final int NUMBER_OF_PRODUCTS = 6;

        List<String> currentAccountNames = CURRENT_ACCOUNT_ARRANGEMENT_TYPE_NAME_MAP.get(GENERAL_RETAIL);

        int numberOfCurrentAccountArrangements = numberOfArrangements <= NUMBER_OF_PRODUCTS
            ? numberOfArrangements : numberOfArrangements - (NUMBER_OF_PRODUCTS - 1);

        IntStream.range(0, numberOfCurrentAccountArrangements).parallel().forEach(randomNumber -> {
            Currency arrangementCurrency = currency == null
                ? getRandomFromList(arrangementCurrencyList)
                : currency;
            int currentAccountNameIndex = randomNumber < currentAccountNames.size() ? randomNumber
                : ThreadLocalRandom.current().nextInt(currentAccountNames.size());

            arrangementsPostRequestBodies
                .add(generateArrangementsPostRequestBody(externalLegalEntityId, 1, arrangementCurrency,
                    GENERAL_RETAIL, currentAccountNames.get(currentAccountNameIndex)));
        });

        if (numberOfArrangements > NUMBER_OF_PRODUCTS) {
            // Any other product than current account
            IntStream.range(2, (NUMBER_OF_PRODUCTS + 1)).parallel().forEach(productId -> {
                Currency arrangementCurrency = currency == null
                ? getRandomFromList(arrangementCurrencyList)
                : currency;
                arrangementsPostRequestBodies.add(
                    generateArrangementsPostRequestBody(externalLegalEntityId, productId, arrangementCurrency,
                        GENERAL_BUSINESS));
            });
        }

        return arrangementsPostRequestBodies;
    }
}
