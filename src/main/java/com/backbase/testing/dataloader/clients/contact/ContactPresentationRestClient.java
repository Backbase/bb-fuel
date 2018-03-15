package com.backbase.testing.dataloader.clients.contact;

import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ContactPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String CONTACT_PRESENTATION_SERVICE = "contact-presentation-service";
    private static final String ENDPOINT_CONTACTS = "/contacts";

    public ContactPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(getGatewayURI() + "/" + CONTACT_PRESENTATION_SERVICE);
    }

    public Response createContact(ContactsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_CONTACTS));
    }
}
