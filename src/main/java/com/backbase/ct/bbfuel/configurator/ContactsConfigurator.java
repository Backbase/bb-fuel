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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactsConfigurator {
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

        log.info("Contacts [{}] ingested for user [{}]", contactsBulkIngestionPostRequestBody.getContacts()
            .size(), externalUserId);
    }
}
