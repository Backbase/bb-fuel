package com.backbase.ct.dataloader.data;

import static com.backbase.ct.dataloader.util.CommonHelpers.generateRandomAmountInRange;
import static java.util.Arrays.asList;

import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionParent;
import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionParent.Type;
import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Random;

public class ActionsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();

    public static ActionRecipesPostRequestBody generateActionRecipesPostRequestBody(String internalArrangementId) {
        // Defined in: https://stash.backbase.com/projects/ACT/repos/actionrecipes-presentation-service/browse/src/main/resources/recipe_specifications_schema.yml
        List<String> specificationIds = asList("1", "4");

        return new ActionRecipesPostRequestBody()
            .withName(faker.lorem().characters(30))
            .withActive(true)
            .withSpecificationId(specificationIds.get(random.nextInt(specificationIds.size())))
            .withActions(createAllActionsList())
            .withAmount(generateRandomAmountInRange(100000L, 999999L))
            .withArrangementId(internalArrangementId)
            .withUserId(null);
    }

    private static List<ActionParent> createAllActionsList() {
        return asList(new ActionParent().withType(Type.NOTIFICATION),
            new ActionParent().withType(Type.EMAIL),
            new ActionParent().withType(Type.SMS));
    }
}
