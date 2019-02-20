package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomAmountInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.util.Arrays.asList;

import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionParent;
import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Random;

public class ActionsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();

    public static ActionRecipesPostRequestBody generateActionRecipesPostRequestBody(String internalArrangementId) {
        List<String> specificationIds = asList("1", "4");

        return new ActionRecipesPostRequestBody()
            .withName(faker.lorem().characters(30))
            .withActive(true)
            .withSpecificationId(getRandomFromList(specificationIds))
            .withActions(createAllActionsList())
            .withAmount(generateRandomAmountInRange(100000L, 999999L))
            .withArrangementId(internalArrangementId)
            .withUserId(null);
    }

    private static List<ActionParent> createAllActionsList() {
        return asList(new ActionParent().withType("NOTIFICATION"),
            new ActionParent().withType("EMAIL"),
            new ActionParent().withType("SMS"));
    }
}
