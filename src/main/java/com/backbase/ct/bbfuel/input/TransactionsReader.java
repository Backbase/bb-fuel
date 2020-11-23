package com.backbase.ct.bbfuel.input;

import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.util.Arrays.asList;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.dbs.transaction.client.v2.model.TransactionsPostRequestBody;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TransactionsReader extends BaseReader {

    public TransactionsPostRequestBody loadSingle(String externalArrangementId) {
        return getRandomFromList(load(globalProperties.getString(CommonConstants.PROPERTY_TRANSACTIONS_DATA_JSON)))
                .id(UUID.randomUUID().toString())
                .arrangementId(externalArrangementId)
                .bookingDate(LocalDate.now());
    }

    /**
     * To be able to find check images in front end apps easily, created transactions should match with
     * transaction-integration-check-images-api
     * And the booking date and value date should be set to today so that we can easily find that transaction and test it.
     */
    public TransactionsPostRequestBody loadSingleWithCheckImages(String externalArrangementId) {
        return getRandomFromList(load(globalProperties.getString(CommonConstants.PROPERTY_TRANSACTIONS_CHECK_IMAGES_DATA_JSON)))
                .arrangementId(externalArrangementId)
                .bookingDate(LocalDate.now())
                .valueDate(LocalDate.now());
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
