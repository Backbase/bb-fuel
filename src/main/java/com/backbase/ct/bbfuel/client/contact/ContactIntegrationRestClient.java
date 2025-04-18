package com.backbase.ct.bbfuel.client.contact;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ContactsBulkPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContactIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_CONTACTS = "/contacts/bulk";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getContactmanager());
        setVersion(SERVICE_VERSION);
    }

    public Response ingestContacts(ContactsBulkPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_CONTACTS));
    }
}
