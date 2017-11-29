package com.backbase.testing.dataloader.clients.contact;

import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ContactPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_CONTACT_PRESENTATION_SERVICE = "/contact-presentation-service/" + SERVICE_VERSION + "/contacts";

    public ContactPresentationRestClient() {
        super(globalProperties.getString(CommonConstants.PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.getString(CommonConstants.PROPERTY_GATEWAY_PATH));
    }

    public Response createContact(ContactsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_CONTACT_PRESENTATION_SERVICE);
    }
}
