package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ActionsDataGenerator.generateActionRecipesPostRequestBody;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACTIONS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ACTIONS_MIN;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.action.ActionRecipesPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionsConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final ActionRecipesPresentationRestClient actionRecipesPresentationRestClient;

    public void ingestActions(String externalUserId) {
        List<ArrangementsByBusinessFunctionGetResponseBody> arrangements = new ArrayList<>();
        int randomAmount = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ACTIONS_MIN),
            globalProperties.getInt(PROPERTY_ACTIONS_MAX));

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        arrangements.addAll(productSummaryPresentationRestClient.getSepaCtArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getUsDomesticWireArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getAchDebitArrangements());

        IntStream.range(0, randomAmount).forEach(randomNumber -> {
            String internalArrangementId = getRandomFromList(arrangements).getId();

            ActionRecipesPostRequestBody actionRecipesPostRequestBody = generateActionRecipesPostRequestBody(
                internalArrangementId);

            actionRecipesPresentationRestClient.createActionRecipe(actionRecipesPostRequestBody)
                .then()
                .statusCode(SC_ACCEPTED);

            log.info("Action ingested with specification id [{}] for arrangement [{}]",
                actionRecipesPostRequestBody.getSpecificationId(), actionRecipesPostRequestBody.getArrangementId());
        });
    }
}
