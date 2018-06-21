package com.backbase.ct.dataloader.configurators;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS;
import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.dataloader.clients.turnovers.CategoriesPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.TransactionsDataGenerator;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.id.CategoryGetResponseBody;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private TransactionsIntegrationRestClient transactionsIntegrationRestClient = new TransactionsIntegrationRestClient();
    private CategoriesPresentationRestClient categoriesPresentationRestClient = new CategoriesPresentationRestClient();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private Random random = new Random();

    public void ingestTransactionsByArrangement(String externalArrangementId) {
        List<TransactionsPostRequestBody> transactions = Collections.synchronizedList(new ArrayList<>());
        List<String> categoryNames = new ArrayList<>();

        if (globalProperties.getBoolean(PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS)) {
            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            categoryNames = categoriesPresentationRestClient.retrieveCategories()
                .stream()
                .map(CategoryGetResponseBody::getCategoryName)
                .collect(Collectors.toList());
        }

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MAX));
        List<String> finalCategoryNames = categoryNames;

        IntStream.range(0, randomAmount).parallel()
            .forEach(randomNumber -> {
                String categoryName = null;
                if (globalProperties.getBoolean(PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS)) {
                    categoryName = finalCategoryNames.get(random.nextInt(finalCategoryNames.size()));
                }

                transactions.add(
                    TransactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId, categoryName));
            });

        transactionsIntegrationRestClient.ingestTransactions(transactions)
            .then()
            .statusCode(SC_CREATED);

        LOGGER.info("Transactions [{}] ingested for arrangement [{}]", randomAmount, externalArrangementId);
    }
}
