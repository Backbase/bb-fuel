package com.backbase.testing.dataloader.clients.messagecenter;

import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.ConversationDraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.ConversationDraftsPostResponseBody;
import com.backbase.dbs.messages.presentation.rest.spec.v3.messagecenter.users.DraftsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class MessagesPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v3";
    private static final String MESSAGES_PRESENTATION_SERVICE = "messages-presentation-service";
    private static final String ENDPOINT_MESSAGE_CENTER = "/message-center";
    private static final String ENDPOINT_DRAFTS = ENDPOINT_MESSAGE_CENTER + "/users/%s/drafts";
    private static final String ENDPOINT_SEND_DRAFT_REQUEST = ENDPOINT_DRAFTS + "/%s/send-draft-request";
    private static final String ENDPOINT_CONVERSATIONS = ENDPOINT_MESSAGE_CENTER + "/users/%s/conversations";
    private static final String ENDPOINT_CONVERSATION_DRAFTS = ENDPOINT_CONVERSATIONS + "/%s/drafts";

    public MessagesPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + MESSAGES_PRESENTATION_SERVICE);
    }

    public Response postDraft(DraftsPostRequestBody body, String externalUserId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(String.format(getPath(ENDPOINT_DRAFTS), externalUserId));
    }

    public Response sendDraftRequest(String externalUserId, String draftId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body("{}")
                .post(String.format(getPath(ENDPOINT_SEND_DRAFT_REQUEST), externalUserId, draftId));
    }

    public Response postConversationDraft(ConversationDraftsPostRequestBody draft, String externalUserId, String conversationId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(draft)
                .post(String.format(getPath(ENDPOINT_CONVERSATION_DRAFTS), externalUserId, conversationId));
    }

    public Response getConversations(String userId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .get(String.format(getPath(ENDPOINT_CONVERSATIONS), userId));
    }
}
