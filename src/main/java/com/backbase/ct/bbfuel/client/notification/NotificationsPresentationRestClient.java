package com.backbase.ct.bbfuel.client.notification;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.client.tokenconverter.InternalTokenRestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationsPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    @Autowired
    private InternalTokenRestClient internalTokenRestClient;

    private static final String SERVICE_API = "service-api/";
    private static final String SERVICE_VERSION = "v2";
    private static final String INTERNAL_TOKEN = "Bearer %s";
    private static final String ENDPOINT_NOTIFICATIONS = "/notifications";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getNotifications());
        setVersion(SERVICE_API + SERVICE_VERSION);
    }

    public Response createNotification(NotificationsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .header(AUTHORIZATION, String.format(INTERNAL_TOKEN,
                internalTokenRestClient.getAuthorisationHeaderForInternalRequest()))
            .body(body)
            .post(getPath(ENDPOINT_NOTIFICATIONS));
    }

}
