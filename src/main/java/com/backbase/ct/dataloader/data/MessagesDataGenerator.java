package com.backbase.ct.dataloader.data;

import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationDraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostRequestBody;
import com.github.javafaker.Faker;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;

public class MessagesDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();

    public static DraftsPostRequestBody generateDraftsPostRequestBody() {
        List<String> categories = Arrays.asList("ma", "ln", "pym");

        return new DraftsPostRequestBody()
            .withBody(encodeString(faker.lorem().paragraph()))
            .withSubject(faker.lorem().sentence().replace(".", ""))
            .withCategory(categories.get(random.nextInt(2)))
            .withImportant(true);
    }

    public static ConversationDraftsPostRequestBody generateConversationDraftsPostRequestBody() {
        return new ConversationDraftsPostRequestBody()
            .withBody(encodeString(faker.lorem().paragraph()));
    }

    private static String encodeString(String value) {
        byte[] bodyEncoded = Base64.encodeBase64(value.getBytes());

        return new String(bodyEncoded);
    }
}
