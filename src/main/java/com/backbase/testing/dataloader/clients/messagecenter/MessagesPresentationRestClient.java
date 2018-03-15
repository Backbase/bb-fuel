package com.backbase.testing.dataloader.clients.messagecenter;

import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.ConversationDraftsPostRequestBody;
import com.backbase.dbs.messages.presentation.rest.spec.v4.messagecenter.DraftsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MessagesPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v4";
    private static final String MESSAGES_PRESENTATION_SERVICE = "messages-presentation-service";
    private static final String ENDPOINT_MESSAGE_CENTER = "/message-center";
    private static final String ENDPOINT_DRAFTS = ENDPOINT_MESSAGE_CENTER + "/drafts";
    private static final String ENDPOINT_SEND_DRAFT_REQUEST = ENDPOINT_DRAFTS + "/%s/send-draft-request";
    private static final String ENDPOINT_CONVERSATIONS = ENDPOINT_MESSAGE_CENTER + "/conversations";
    private static final String ENDPOINT_CONVERSATION_DRAFTS = ENDPOINT_CONVERSATIONS + "/%s/drafts";

    public MessagesPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(getGatewayURI() + "/" + MESSAGES_PRESENTATION_SERVICE);
    }

    public Response postDraft(DraftsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_DRAFTS));
    }

    public Response sendDraftRequest(String draftId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body("{}")
            .post(String.format(getPath(ENDPOINT_SEND_DRAFT_REQUEST), draftId));
    }

    public Response postConversationDraft(ConversationDraftsPostRequestBody draft, String conversationId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(draft)
            .post(String.format(getPath(ENDPOINT_CONVERSATION_DRAFTS), conversationId));
    }

    public Response getConversations() {
        return requestSpec()
            .contentType(ContentType.JSON)
            .get(getPath(ENDPOINT_CONVERSATIONS));
    }
}
