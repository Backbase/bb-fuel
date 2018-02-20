package com.backbase.testing.dataloader.configurators;

import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.ConversationDraftsPostResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.ConversationsGetResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.DraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.DraftsPostResponseBody;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.messagecenter.MessagesPresentationRestClient;
import com.backbase.testing.dataloader.data.MessagesDataGenerator;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONVERSATIONS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONVERSATIONS_MIN;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static com.backbase.testing.dataloader.data.MessagesDataGenerator.generateConversationDraftsPostRequestBody;
import static com.backbase.testing.dataloader.data.MessagesDataGenerator.generateDraftsPostRequestBody;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

public class MessagesConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private LoginRestClient loginRestClient = new LoginRestClient();
    private MessagesPresentationRestClient messagesPresentationRestClient = new MessagesPresentationRestClient();

    public void ingestConversations(String externalUserId) {
        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_CONVERSATIONS_MIN), globalProperties.getInt(PROPERTY_CONVERSATIONS_MAX));
        IntStream.range(0, randomAmount).forEach(randomNumber -> {

            loginRestClient.login(externalUserId, externalUserId);
            DraftsPostRequestBody draftsPostRequestBody = generateDraftsPostRequestBody();
            String draftId = messagesPresentationRestClient.postDraft(draftsPostRequestBody)
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .extract()
                    .as(DraftsPostResponseBody.class)
                    .getId();

            messagesPresentationRestClient.sendDraftRequest(draftId)
                    .then()
                    .statusCode(SC_ACCEPTED);

            loginRestClient.login(USER_ADMIN, USER_ADMIN);
            String conversationId = messagesPresentationRestClient.getConversations()
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(ConversationsGetResponseBody.class)
                    .getConversations()
                    .get(0)
                    .getId();

            String conversationDraftId = messagesPresentationRestClient.postConversationDraft(generateConversationDraftsPostRequestBody(), conversationId)
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .extract()
                    .as(ConversationDraftsPostResponseBody.class)
                    .getId();

            messagesPresentationRestClient.sendDraftRequest(conversationDraftId)
                    .then()
                    .statusCode(SC_ACCEPTED);

            LOGGER.info(String.format("Conversation ingested with subject [%s] for user [%s]", draftsPostRequestBody.getSubject(), externalUserId));
        });
    }
}