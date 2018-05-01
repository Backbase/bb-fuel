package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.contact.ContactPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.ContactsDataGenerator;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static org.apache.http.HttpStatus.SC_CREATED;

public class ContactsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private ContactPresentationRestClient contactPresentationRestClient = new ContactPresentationRestClient();

    public void ingestContacts(String externalUserId) {
        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_CONTACTS_MIN), globalProperties.getInt(CommonConstants.PROPERTY_CONTACTS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            ContactsPostRequestBody contact = ContactsDataGenerator.generateContactsPostRequestBody();

            loginRestClient.login(externalUserId, externalUserId);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

            contactPresentationRestClient.createContact(contact)
                    .then()
                    .statusCode(SC_CREATED);

            LOGGER.info("Contact ingested with name [{}] for user [{}]", contact.getName(), externalUserId);
        });
    }
}