//package com.backbase.testing.dataloader.data;
//
//import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
//import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.Action_;
//import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.Selector_;
//import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.Trigger_;
//import com.backbase.testing.dataloader.utils.CommonHelpers;
//import com.github.javafaker.Faker;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.UUID;
//
//public class ActionRecipesDataGenerator {
//
//    private Faker faker = new Faker();
//    private Random random = new Random();
//
//    public ActionRecipesPostRequestBody generateActionRecipesPostRequestBody(String externalArrangementId) {
//        Map<String, List> filter = new HashMap<>();
//        Map<String, List> conditions = new HashMap<>();
//
//        filter.put("and", )
//
//        {
//            "and": [
//            {
//                "eq": [
//                {
//                    "pathValue": [
//                    "transaction.creditDebitIndicator"
//                            ]
//                },"CRDT"]
//            },
//            {
//                "gte": [
//                {
//                    "pathValue": [
//                    "transaction.amount"
//                            ]
//                },
//                100000
//                    ]
//            }
//            ]
//        }
//
//        filter.put("gt", Collections.singletonList(new Selector_()
//                .withPath("transaction.amount")
//                .withValue(CommonHelpers.generateRandomAmountInRange(10000, 99999).toString())));
//
//        return new ActionRecipesPostRequestBody()
//                .withName(faker.lorem().sentence().replace(".", ""))
//                .withSpecificationId(UUID.randomUUID().toString())
//                .withActive(true)
//                .withTrigger(new Trigger_()
//                        .withSelectors(Collections.singletonList(new Selector_()
//                        .withPath("transaction.arrangementId")
//                        .withValue(externalArrangementId)))
//                        .withFilter(filter))
//                .withActions(Collections.singletonList(new Action_()
//                        .withType("notification")));
//    }
//}
