package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.TransactionsDataGenerator;
import com.backbase.ct.bbfuel.input.TransactionsReader;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.pocket.tailor.client.v2.model.Pocket;
import com.backbase.dbs.transaction.client.v2.model.TransactionsPostRequestBody;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsConfigurator {

    public static final int NUMBER_OF_POCKET_TRANSACTIONS = 5;

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final TransactionsReader reader = new TransactionsReader();

    private final TransactionsIntegrationRestClient transactionsIntegrationRestClient;

    public void ingestTransactionsByArrangement(String externalArrangementId, boolean isRetail) {
        List<TransactionsPostRequestBody> transactions = Collections.synchronizedList(new ArrayList<>());

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MAX));

        if (isRetail) {
            // Add 1 check images per account.
            transactions.add(reader.loadSingleWithCheckImages(externalArrangementId));

            // After that ingest rest of the transactions.
            IntStream.range(0, randomAmount).parallel()
                    .forEach(randomNumber -> transactions.add(
                            reader.loadSingle(externalArrangementId)));
        } else {
            IntStream.range(0, randomAmount).parallel()
                    .forEach(randomNumber -> transactions.add(
                            TransactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId)));
        }
        transactionsIntegrationRestClient.ingestTransactions(transactions)
            .then()
            .statusCode(SC_CREATED);

        log.info("Transactions [{}] ingested for arrangement [{}]", randomAmount, externalArrangementId);
    }

    /**
     * Ingest transactions that will be referenced to the specified pockets.
     * @param externalArrangementId the external arrangement id of the parent pocket arrangement
     * @param ingestedPockets list of ingested pockets
     */
    public void ingestTransactionsForPocket(String externalArrangementId, List<Pocket> ingestedPockets) {
        Builder<TransactionsPostRequestBody> transactionsPostRequestBodyBuilder = ImmutableList.builder();

        for (Pocket pocket : ingestedPockets) {
            transactionsPostRequestBodyBuilder.addAll(reader.loadWithPocketAsReference(externalArrangementId, pocket.getArrangementId()));
        }

        List<TransactionsPostRequestBody> transactionsPostRequestBodyList = transactionsPostRequestBodyBuilder.build();
        transactionsIntegrationRestClient.ingestTransactions(transactionsPostRequestBodyList)
            .then()
            .statusCode(SC_CREATED);

        log.info("Ingested [{}] transactions for parent pocket external arrangement [{}]", transactionsPostRequestBodyList.size(),
            externalArrangementId);
    }

    /**
     * Ingest transactions that counterbalance the pocket transactions for the current arrangement.
     * @param currentAccountExternalArrangementId the external arrangement id of the current arrangement
     * @param parentPocketExternalArrangementId  the external arrangement id of the parent pocket arrangement
     */
     public void ingestTransactionsForCurrentAccount(String currentAccountExternalArrangementId, String parentPocketExternalArrangementId) {
        Builder<TransactionsPostRequestBody> transactionsPostRequestBodyBuilder = ImmutableList.builder();

         for (int i = 0; i < NUMBER_OF_POCKET_TRANSACTIONS; i++) {
             transactionsPostRequestBodyBuilder.addAll(reader.loadWithPocketParentAsReference(currentAccountExternalArrangementId, parentPocketExternalArrangementId));
         }

        List<TransactionsPostRequestBody> transactionsPostRequestBodyList = transactionsPostRequestBodyBuilder.build();
        transactionsIntegrationRestClient.ingestTransactions(transactionsPostRequestBodyList)
            .then()
            .statusCode(SC_CREATED);

        log.info("Ingested [{}] transactions for current account external arrangement [{}]", transactionsPostRequestBodyList.size(),
            currentAccountExternalArrangementId);
    }
}
