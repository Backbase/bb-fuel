package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.util.Arrays.asList;

import com.backbase.dbs.action.client.v2.model.ActionRecipesPostRequestBodyParent;
import com.backbase.dbs.action.client.v2.model.ActionParent;
import com.backbase.dbs.action.client.v2.model.ActionRecipeItemParent;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Random;

public class ActionsDataGenerator {

    private static Faker faker = new Faker();

    public static ActionRecipeItemParent generateActionRecipesPostRequestBody(String internalArrangementId) {
        List<String> specificationIds = asList("1", "4");

        return new ActionRecipesPostRequestBodyParent()
                .specificationId(getRandomFromList(specificationIds))
                .actions(createAllActionsList())
                .amount(generateRandomAmountInRange(100000L, 999999L).toString())
                .arrangementId(internalArrangementId)
                .userId(null)
                .active(true)
                .name(faker.lorem().characters(30));
    }

    private static List<ActionParent> createAllActionsList() {
        return asList(new ActionParent().type("NOTIFICATION"),
            new ActionParent().type("EMAIL"),
            new ActionParent().type("SMS"));
    }
}
