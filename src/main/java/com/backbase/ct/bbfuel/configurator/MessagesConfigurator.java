package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGES_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGES_MIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGE_TOPICS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGE_TOPICS_MIN;
import static com.backbase.ct.bbfuel.data.MessagesDataGenerator.generateConversationDraftsPostRequestBody;
import static com.backbase.ct.bbfuel.data.MessagesDataGenerator.generateDraftsPostRequestBody;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static java.util.Collections.singleton;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.messagecenter.MessagesPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.data.MessagesDataGenerator;
import com.backbase.ct.bbfuel.service.LegalEntityService;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.messages.rest.spec.v4.messagecenter.DraftPostResponseBody;
import com.backbase.dbs.messages.rest.spec.v4.messagecenter.MessageDraftsGetResponseBody;
import com.backbase.dbs.messages.rest.spec.v4.messagecenter.MessageDraftsPostRequestBody;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagesConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final LoginRestClient loginRestClient;
    private final MessagesPresentationRestClient messagesPresentationRestClient;
    private final LegalEntityService legalEntityService;
    private final UserPresentationRestClient userPresentationRestClient;

    public void ingestConversations(String externalUserId) {
        int howManyMessages = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_MESSAGES_MIN),
                globalProperties.getInt(PROPERTY_MESSAGES_MAX));

        int howManyTopics = CommonHelpers.generateRandomNumberInRange(
            globalProperties.getInt(PROPERTY_MESSAGE_TOPICS_MIN),globalProperties.getInt(PROPERTY_MESSAGE_TOPICS_MAX));

        loginRestClient.loginBankAdmin();

        List<String> topicIds = new ArrayList<>();
        String bankAdmin = legalEntityService.getRootAdmin();
        String bankAdminInternalId = userPresentationRestClient.getUserByExternalId(bankAdmin).getId();

        IntStream.range(0, howManyTopics).forEach(number -> {
            String topicId = messagesPresentationRestClient.postTopic(MessagesDataGenerator
                .generateTopicPostRequestBody(singleton(bankAdminInternalId)))
                .then()
                .statusCode(SC_ACCEPTED)
                .extract()
                .path("id");
            topicIds.add(topicId);

            log.info("Topic ingested with id [{}] for subscriber [{}]", topicId, bankAdmin);
        });

        IntStream.range(0, howManyMessages).forEach(number -> {
            loginRestClient.login(externalUserId, externalUserId);

            MessageDraftsPostRequestBody draftsPostRequestBody = generateDraftsPostRequestBody(getRandomFromList(topicIds));
            String draftId = messagesPresentationRestClient.postDraft(draftsPostRequestBody)
                .then()
                .statusCode(SC_ACCEPTED)
                .extract()
                .as(DraftPostResponseBody.class)
                .getId();

            messagesPresentationRestClient.sendDraftRequest(draftId)
                .then()
                .statusCode(SC_ACCEPTED);

            loginRestClient.loginBankAdmin();
            String conversationId = messagesPresentationRestClient.getConversations()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(DraftPostResponseBody[].class)[0]
                .getId();

            String conversationDraftId = messagesPresentationRestClient
                .postConversationDraft(generateConversationDraftsPostRequestBody(), conversationId)
                .then()
                .statusCode(SC_ACCEPTED)
                .extract()
                .as(MessageDraftsGetResponseBody.class)
                .getId();

            messagesPresentationRestClient.sendDraftRequest(conversationDraftId)
                .then()
                .statusCode(SC_ACCEPTED);

            log.info("Conversation ingested with subject [{}] for user [{}]", draftsPostRequestBody.getSubject(),
                externalUserId);
        });
    }
}
