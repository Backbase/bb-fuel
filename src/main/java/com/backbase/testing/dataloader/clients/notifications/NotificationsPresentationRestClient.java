package com.backbase.testing.dataloader.clients.notifications;

import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class NotificationsPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String NOTIFICATIONS_PRESENTATION_SERVICE = "notifications-presentation-service";
    private static final String ENDPOINT_NOTIFICATIONS = "/notifications";

    public NotificationsPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(USE_LOCAL ? LOCAL_GATEWAY : GATEWAY + "/" + NOTIFICATIONS_PRESENTATION_SERVICE);
    }

    public Response createNotification(NotificationsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_NOTIFICATIONS));
    }
}
