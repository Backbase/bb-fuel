package com.backbase.ct.dataloader.configurators;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_CONTACTS_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_CONTACTS_MIN;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_CONTACT_ACCOUNTS_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_CONTACT_ACCOUNTS_MIN;
import static com.backbase.ct.dataloader.data.ContactsDataGenerator.generateContactsBulkIngestionPostRequestBody;
import static com.backbase.ct.dataloader.utils.CommonHelpers.generateRandomNumberInRange;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.contact.ContactIntegrationRestClient;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.bulk.ContactsBulkIngestionPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private ContactIntegrationRestClient contactIntegrationRestClient = new ContactIntegrationRestClient();

    public void ingestContacts(String externalUserId) {
        int numberOfContacts = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_CONTACTS_MIN),
            globalProperties.getInt(PROPERTY_CONTACTS_MAX));
        int numberOfAccountsPerContact = generateRandomNumberInRange(
            globalProperties.getInt(PROPERTY_CONTACT_ACCOUNTS_MIN),
            globalProperties.getInt(PROPERTY_CONTACT_ACCOUNTS_MAX));

        ContactsBulkIngestionPostRequestBody contactsBulkIngestionPostRequestBody = generateContactsBulkIngestionPostRequestBody(
            externalUserId, numberOfContacts, numberOfAccountsPerContact);

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        contactIntegrationRestClient.ingestContacts(contactsBulkIngestionPostRequestBody)
            .then()
            .statusCode(SC_CREATED);

        LOGGER.info("Contacts [{}] ingested for user [{}]", contactsBulkIngestionPostRequestBody.getContacts()
            .size(), externalUserId);
    }
}
