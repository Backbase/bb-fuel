package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationDraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.TopicsPostRequestBody;
import com.github.javafaker.Faker;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

public class MessagesDataGenerator {

    private static Faker faker = new Faker();

    public static DraftsPostRequestBody generateDraftsPostRequestBody(List<String> topicIds) {
        return new DraftsPostRequestBody()
                .withBody(encodeString(faker.lorem().paragraph()))
                .withSubject(faker.lorem().sentence().replace(".", ""))
                .withCategory(getRandomFromList(topicIds))
                .withImportant(true);
    }

    public static ConversationDraftsPostRequestBody generateConversationDraftsPostRequestBody() {
        return new ConversationDraftsPostRequestBody()
                .withBody(encodeString(faker.lorem().paragraph()));
    }

    public static TopicsPostRequestBody generateTopicPostRequestBody() {
        return new TopicsPostRequestBody()
                .withName(faker.lorem().sentence(2, 2))
                .withSubscribers(asSet("admin"));
    }

    private static String encodeString(String value) {
        byte[] bodyEncoded = Base64.encodeBase64(value.getBytes());

        return new String(bodyEncoded);
    }
}
