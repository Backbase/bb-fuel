package com.backbase.testing.dataloader.clients.notifications;

import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class NotificationsPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_NOTIFICATIONS_PRESENTATION_SERVICE = "/notifications-presentation-service/" + SERVICE_VERSION + "/notifications";

    public NotificationsPresentationRestClient() {
        super(globalProperties.getString(CommonConstants.PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.getString(CommonConstants.PROPERTY_GATEWAY_PATH));
    }

    public Response createNotification(NotificationsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_NOTIFICATIONS_PRESENTATION_SERVICE);
    }
}
