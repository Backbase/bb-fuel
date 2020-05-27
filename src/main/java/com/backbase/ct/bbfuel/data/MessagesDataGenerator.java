package com.backbase.ct.bbfuel.data;

import com.backbase.dbs.messages.rest.spec.v4.messagecenter.ConversationMessageDraftPostRequestBody;
import com.backbase.dbs.messages.rest.spec.v4.messagecenter.MessageDraftsPostRequestBody;
import com.backbase.dbs.messages.rest.spec.v4.messagecenter.TopicsPostRequestBody;
import com.github.javafaker.Faker;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;

public class MessagesDataGenerator {

    private static Faker faker = new Faker();

    public static MessageDraftsPostRequestBody generateDraftsPostRequestBody(String topicId) {
        return new MessageDraftsPostRequestBody()
            .withBody(encodeString(faker.lorem().paragraph()))
            .withSubject(faker.lorem().sentence().replace(".", ""))
            .withTopic(topicId);
    }

    public static ConversationMessageDraftPostRequestBody generateConversationDraftsPostRequestBody() {
        return new ConversationMessageDraftPostRequestBody()
            .withBody(encodeString(faker.lorem().paragraph()));
    }

    public static TopicsPostRequestBody generateTopicPostRequestBody(Set<String> subscribers) {
        return new TopicsPostRequestBody()
            .withName(faker.lorem().sentence(2, 2))
            .withSubscribers(subscribers);
    }

    private static String encodeString(String value) {
        byte[] bodyEncoded = Base64.encodeBase64(value.getBytes());

        return new String(bodyEncoded);
    }
}
