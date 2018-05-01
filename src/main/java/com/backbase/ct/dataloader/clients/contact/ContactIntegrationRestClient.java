package com.backbase.ct.dataloader.clients.contact;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsBulkIngestionPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ContactIntegrationRestClient extends AbstractRestClient {

    private static final String CONTACT_MANAGER = globalProperties.getString(CommonConstants.PROPERTY_CONTACT_MANAGER_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String CONTACT_INTEGRATION_SERVICE = "contact-integration-service";
    private static final String ENDPOINT_CONTACTS = "/contacts/bulk";

    public ContactIntegrationRestClient() {
        super(CONTACT_MANAGER, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response ingestContacts(ContactsBulkIngestionPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_CONTACTS));
    }

    @Override
    protected String composeInitialPath() {
        return CONTACT_INTEGRATION_SERVICE;
    }

}
