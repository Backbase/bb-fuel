package com.backbase.ct.dataloader.data;

import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsBulkIngestionPostRequestBody;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ExternalAccessContext;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ExternalAccountInformation;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ExternalContact;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.backbase.ct.dataloader.data.ProductSummaryDataGenerator.generateRandomIban;

public class ContactsDataGenerator {

    private static Faker faker = new Faker();

    public static ContactsBulkIngestionPostRequestBody generateContactsBulkIngestionPostRequestBody(String externalUserId, int numberOfContacts, int numberOfAccountsPerContact) {
        return new ContactsBulkIngestionPostRequestBody()
            .withAccessContext(new ExternalAccessContext()
                .withExternalServiceAgreementId(null)
                .withExternalLegalEntityId(null)
                .withExternalUserId(externalUserId))
            .withContacts(generateExternalContacts(numberOfContacts, numberOfAccountsPerContact));
    }

    private static List<ExternalContact> generateExternalContacts(int numberOfContacts, int numberOfAccountsPerContact) {
        List<ExternalContact> contacts = new ArrayList<>();

        for (int i = 0; i < numberOfContacts; i++) {
            contacts.add(generateExternalContact(numberOfAccountsPerContact));
        }

        return contacts;
    }


    private static ExternalContact generateExternalContact(int numberOfAccounts) {
        List<ExternalAccountInformation> accounts = new ArrayList<>();

        for (int i = 0; i < numberOfAccounts; i++) {
            accounts.add(new ExternalAccountInformation()
                .withExternalId(UUID.randomUUID().toString().substring(0, 32))
                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                .withIBAN(generateRandomIban())
                .withAccountNumber(faker.finance().iban())
                .withAlias(faker.lorem().characters(10))
                .withBIC(faker.finance().bic())
                .withAccountHolderAddressLine1(faker.address().streetAddress())
                .withAccountHolderAddressLine2(faker.address().secondaryAddress())
                .withAccountHolderStreetName(faker.address().streetAddress())
                .withAccountHolderPostCode(faker.address().zipCode())
                .withAccountHolderTown(faker.address().cityName())
                .withAccountHolderCountry(faker.address().countryCode())
                .withAccountHolderCountrySubDivision(faker.address().state())
                .withBankCode(faker.lorem().characters(10))
                .withBankAddressLine1(faker.address().streetAddress())
                .withBankAddressLine2(faker.address().secondaryAddress())
                .withBankStreetName(faker.address().streetAddress())
                .withBankPostCode(faker.address().zipCode())
                .withBankTown(faker.address().city())
                .withBankCountry(faker.address().countryCode())
                .withBankCountrySubDivision(faker.address().state()));
        }

        return new ExternalContact()
            .withExternalId(UUID.randomUUID().toString().substring(0, 32))
            .withName(faker.name().fullName())
            .withAlias(faker.lorem().characters(10))
            .withContactPerson(faker.name().fullName())
            .withEmailId(faker.internet().emailAddress())
            .withPhoneNumber(faker.phoneNumber().phoneNumber())
            .withCategory(faker.lorem().characters(10))
            .withAddressLine1(faker.address().streetAddress())
            .withAddressLine2(faker.address().secondaryAddress())
            .withStreetName(faker.address().streetAddress())
            .withPostCode(faker.address().zipCode())
            .withTown(faker.address().city())
            .withCountry(faker.address().countryCode())
            .withCountrySubDivision(faker.address().state())
            .withAccounts(accounts);
    }
}
