package com.backbase.ct.dataloader.clients.notifications;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class NotificationsPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String NOTIFICATIONS_PRESENTATION_SERVICE = "notifications-presentation-service";
    private static final String ENDPOINT_NOTIFICATIONS = "/notifications";

    public NotificationsPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response createNotification(NotificationsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_NOTIFICATIONS));
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + NOTIFICATIONS_PRESENTATION_SERVICE;
    }

}
