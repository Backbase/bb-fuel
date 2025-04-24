package com.backbase.ct.bbfuel.data;

import com.backbase.dbs.message.service.api.v5.model.TopicsPostRequestBody;
import com.github.javafaker.Faker;
import java.util.List;

public class MessagesDataGenerator {

    private static Faker faker = new Faker();

    public static TopicsPostRequestBody generateTopicPostRequestBody(List<String> subscribers) {
        return new TopicsPostRequestBody()
            .withName(faker.lorem().sentence(2, 2))
            .withSubscribers(subscribers);
    }
}
