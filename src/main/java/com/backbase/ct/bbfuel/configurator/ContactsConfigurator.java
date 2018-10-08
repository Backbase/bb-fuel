package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_MIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACT_ACCOUNTS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACT_ACCOUNTS_MIN;
import static com.backbase.ct.bbfuel.data.ContactsDataGenerator.generateContactsBulkIngestionPostRequestBody;
import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.contact.ContactIntegrationRestClient;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ContactsBulkIngestionPostRequestBody;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final ContactIntegrationRestClient contactIntegrationRestClient;

    public void ingestContacts(String externalServiceAgreementId, String externalUserId) {
        int numberOfContacts = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_CONTACTS_MIN),
            globalProperties.getInt(PROPERTY_CONTACTS_MAX));
        int numberOfAccountsPerContact = generateRandomNumberInRange(
            globalProperties.getInt(PROPERTY_CONTACT_ACCOUNTS_MIN),
            globalProperties.getInt(PROPERTY_CONTACT_ACCOUNTS_MAX));

        ContactsBulkIngestionPostRequestBody contactsBulkIngestionPostRequestBody = generateContactsBulkIngestionPostRequestBody(
            externalServiceAgreementId, externalUserId, numberOfContacts, numberOfAccountsPerContact);

        contactIntegrationRestClient.ingestContacts(contactsBulkIngestionPostRequestBody)
            .then()
            .statusCode(SC_CREATED);

        LOGGER.info("Contacts [{}] ingested for user [{}]", contactsBulkIngestionPostRequestBody.getContacts()
            .size(), externalUserId);
    }
}
