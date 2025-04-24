package com.backbase.ct.bbfuel.client.messagecenter;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.message.service.api.v5.model.TopicsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagesPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v4";
    private static final String ENDPOINT_TOPICS = "/employee/topics";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getMessages() + "/" + CLIENT_API);
    }

    public Response postTopic(TopicsPostRequestBody topicsPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(topicsPostRequestBody)
            .post(getPath(ENDPOINT_TOPICS));
    }
}
