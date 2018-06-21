package com.backbase.ct.dataloader.configurators;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_CONVERSATIONS_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_CONVERSATIONS_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.messagecenter.MessagesPresentationRestClient;
import com.backbase.ct.dataloader.data.MessagesDataGenerator;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationDraftsPostResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationsGetResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostResponseBody;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagesConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private LoginRestClient loginRestClient = new LoginRestClient();
    private MessagesPresentationRestClient messagesPresentationRestClient = new MessagesPresentationRestClient();

    public void ingestConversations(String externalUserId) {
        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(PROPERTY_CONVERSATIONS_MIN),
                globalProperties.getInt(PROPERTY_CONVERSATIONS_MAX));
        IntStream.range(0, randomAmount).forEach(randomNumber -> {

            loginRestClient.login(externalUserId, externalUserId);
            DraftsPostRequestBody draftsPostRequestBody = MessagesDataGenerator.generateDraftsPostRequestBody();
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
