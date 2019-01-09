package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGES_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGES_MIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGE_TOPICS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGE_TOPICS_MIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static java.util.Collections.singleton;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.messagecenter.MessagesPresentationRestClient;
import com.backbase.ct.bbfuel.data.MessagesDataGenerator;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationDraftsPostResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationsGetResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostResponseBody;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagesConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final LoginRestClient loginRestClient;
    private final MessagesPresentationRestClient messagesPresentationRestClient;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    public void ingestConversations(String externalUserId) {
        int howManyMessages = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_MESSAGES_MIN),
                globalProperties.getInt(PROPERTY_MESSAGES_MAX));

        int howManyTopics = CommonHelpers.generateRandomNumberInRange(
            globalProperties.getInt(PROPERTY_MESSAGE_TOPICS_MIN),globalProperties.getInt(PROPERTY_MESSAGE_TOPICS_MAX));

        loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);

        List<String> topicIds = new ArrayList<>();

        IntStream.range(0, howManyTopics).forEach(number -> {
            String topicId = messagesPresentationRestClient.postTopic(MessagesDataGenerator
                .generateTopicPostRequestBody(singleton(rootEntitlementsAdmin)))
                .then()
                .statusCode(SC_ACCEPTED)
                .extract()
                .path("id");
            topicIds.add(topicId);

            LOGGER.info("Topic ingested with id [{}] for subscriber [{}]", topicId, rootEntitlementsAdmin);
        });

        IntStream.range(0, howManyMessages).forEach(number -> {
            loginRestClient.login(externalUserId, externalUserId);

            DraftsPostRequestBody draftsPostRequestBody = MessagesDataGenerator.generateDraftsPostRequestBody(topicIds);
            String draftId = messagesPresentationRestClient.postDraft(draftsPostRequestBody)
                .then()
                .statusCode(SC_ACCEPTED)
                .extract()
                .as(DraftsPostResponseBody.class)
                .getId();

            messagesPresentationRestClient.sendDraftRequest(draftId)
                .then()
                .statusCode(SC_ACCEPTED);

            loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            String conversationId = messagesPresentationRestClient.getConversations()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ConversationsGetResponseBody[].class)[0]
                .getId();

            String conversationDraftId = messagesPresentationRestClient
                .postConversationDraft(MessagesDataGenerator.generateConversationDraftsPostRequestBody(),
                    conversationId)
                .then()
                .statusCode(SC_ACCEPTED)
                .extract()
                .as(ConversationDraftsPostResponseBody.class)
                .getId();

            messagesPresentationRestClient.sendDraftRequest(conversationDraftId)
                .then()
                .statusCode(SC_ACCEPTED);

            LOGGER.info("Conversation ingested with subject [{}] for user [{}]", draftsPostRequestBody.getSubject(),
                externalUserId);
        });
    }
}
