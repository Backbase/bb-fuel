package com.backbase.ct.bbfuel.data;

import com.backbase.dbs.messages.rest.spec.v4.messagecenter.TopicsPostRequestBody;
import com.github.javafaker.Faker;
import java.util.Set;

public class MessagesDataGenerator {

    private static Faker faker = new Faker();

    public static TopicsPostRequestBody generateTopicPostRequestBody(Set<String> subscribers) {
        return new TopicsPostRequestBody()
            .withName(faker.lorem().sentence(2, 2))
            .withSubscribers(subscribers);
    }
}
