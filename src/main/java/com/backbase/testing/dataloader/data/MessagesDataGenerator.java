package com.backbase.testing.dataloader.data;

import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.ConversationDraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.DraftsPostRequestBody;
import com.github.javafaker.Faker;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MessagesDataGenerator {

    private Faker faker = new Faker();
    private Random random = new Random();

    public DraftsPostRequestBody generateDraftsPostRequestBody() {
        List<String> categories = Arrays.asList("ma", "ln", "pym");

        return new DraftsPostRequestBody()
                .withBody(encodeString(faker.lorem().paragraph()))
                .withSubject(faker.lorem().sentence().replace(".", ""))
                .withCategory(categories.get(random.nextInt(2)))
                .withImportant(true);
    }

    public ConversationDraftsPostRequestBody generateConversationDraftsPostRequestBody() {
        return new ConversationDraftsPostRequestBody()
                .withBody(encodeString(faker.lorem().paragraph()));
    }

    private String encodeString(String value) {
        byte[] bodyEncoded = Base64.encodeBase64(value.getBytes());

        return new String(bodyEncoded);
    }
}
