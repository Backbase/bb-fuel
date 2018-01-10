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

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONVERSATIONS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONVERSATIONS_MIN;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

public class MessagesConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private LoginRestClient loginRestClient = new LoginRestClient();
    private MessagesPresentationRestClient messagesPresentationRestClient = new MessagesPresentationRestClient();
    private MessagesDataGenerator messagesDataGenerator = new MessagesDataGenerator();

    public void ingestConversations(String externalUserId) {
        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_CONVERSATIONS_MIN), globalProperties.getInt(PROPERTY_CONVERSATIONS_MAX)); i++) {

            DraftsPostRequestBody draftsPostRequestBody = messagesDataGenerator.generateDraftsPostRequestBody();
            String draftId = messagesPresentationRestClient.postDraft(draftsPostRequestBody, externalUserId)
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .extract()
                    .as(DraftsPostResponseBody.class)
                    .getId();

            messagesPresentationRestClient.sendDraftRequest(externalUserId, draftId)
                    .then()
                    .statusCode(SC_ACCEPTED);

            String conversationId = messagesPresentationRestClient.getConversations(USER_ADMIN)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(ConversationsGetResponseBody.class)
                    .getConversations()
                    .get(0)
                    .getId();

            String conversationDraftId = messagesPresentationRestClient.postConversationDraft(messagesDataGenerator.generateConversationDraftsPostRequestBody(), USER_ADMIN, conversationId)
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .extract()
                    .as(ConversationDraftsPostResponseBody.class)
                    .getId();

            messagesPresentationRestClient.sendDraftRequest(USER_ADMIN, conversationDraftId)
                    .then()
                    .statusCode(SC_ACCEPTED);

            LOGGER.info(String.format("Conversation ingested with subject [%s] for user [%s]", draftsPostRequestBody.getSubject(), externalUserId));
        }
    }
}