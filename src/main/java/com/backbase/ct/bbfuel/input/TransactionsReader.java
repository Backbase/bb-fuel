package com.backbase.ct.bbfuel.input;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.util.Arrays.asList;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TransactionsReader extends BaseReader {

    private static Faker faker = new Faker();

    public TransactionsPostRequestBody loadSingle(String externalArrangementId) {
        return getRandomFromList(load(globalProperties.getString(CommonConstants.PROPERTY_TRANSACTIONS_DATA_JSON)))
                .withId(UUID.randomUUID().toString())
                .withArrangementId(externalArrangementId)
                .withBookingDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-180, 0)));
    }

    /**
     * To be able to retrieve check images for a transaction, we need at least one of the transactions with
     * with transactions id = TRANS0000000000001
     * We need to set the date of the transaction to today so that we can easly find that transaction and test it.
     */
    public static TransactionsPostRequestBody loadSingleWithCheckImages(String externalArrangementId) {
        TransactionsPostRequestBody defaultBody = this.loadSingle(externalArrangementId);
        return defaultBody.withId("TRANS0000000000001")
                .withCreditDebitIndicator(DBIT)
                .withBookingDate(new Date())
                .withValueDate(new Date())
                .withCheckSerialNumber(new BigDecimal(generateRandomNumberInRange(1, 99999999)));
    }

    private List<TransactionsPostRequestBody> load(String uri) {
        List<TransactionsPostRequestBody> transactions;

        try {
            TransactionsPostRequestBody[] parsedTransactions = ParserUtil.convertJsonToObject(uri, TransactionsPostRequestBody[].class);
            transactions = asList(parsedTransactions);
        } catch(IOException e) {
            log.error("Failed parsing file with Transactions", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return transactions;
    }
}
