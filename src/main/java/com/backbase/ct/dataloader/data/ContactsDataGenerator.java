package com.backbase.ct.dataloader.data;

import static com.backbase.ct.dataloader.data.ProductSummaryDataGenerator.generateRandomIban;

import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.AccessContext;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.Address;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ContactsBulkIngestionPostRequestBody;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ExternalAccountInformation;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ExternalContact;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContactsDataGenerator {

    private static Faker faker = new Faker();

    public static ContactsBulkIngestionPostRequestBody generateContactsBulkIngestionPostRequestBody(
        String externalServiceAgreementId, String externalUserId, int numberOfContacts, int numberOfAccountsPerContact) {
        return new ContactsBulkIngestionPostRequestBody()
            .withAccessContext(new AccessContext()
                .withExternalServiceAgreementId(externalServiceAgreementId)
                .withExternalLegalEntityId(null)
                .withExternalUserId(externalUserId)
                .withScope(AccessContext.Scope.SA))
            .withContacts(generateContacts(numberOfContacts, numberOfAccountsPerContact));
    }

    private static List<ExternalContact> generateContacts(int numberOfContacts, int numberOfAccountsPerContact) {
        List<ExternalContact> contacts = new ArrayList<>();

        for (int i = 0; i < numberOfContacts; i++) {
            contacts.add(generateContact(numberOfAccountsPerContact));
        }

        return contacts;
    }

    private static ExternalContact generateContact(int numberOfAccounts) {
        List<ExternalAccountInformation> accounts = new ArrayList<>();

        for (int i = 0; i < numberOfAccounts; i++) {
            accounts.add(new ExternalAccountInformation()
                .withExternalId(UUID.randomUUID().toString().substring(0, 32))
                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                .withIban(generateRandomIban())
                .withAccountNumber(faker.finance().iban())
                .withAlias(faker.lorem().characters(10))
                .withBic(null)
                .withAccountHolderAddress(new Address()
                    .withAddressLine1(faker.address().streetAddress())
                    .withAddressLine2(faker.address().secondaryAddress())
                    .withStreetName(faker.address().streetAddress())
                    .withPostCode(faker.address().zipCode())
                    .withTown(faker.address().cityName())
                    .withCountry(faker.address().countryCode())
                    .withCountrySubDivision(faker.address().state()))
                .withBankCode(faker.lorem().characters(10))
                .withBankAddress(new Address()
                    .withAddressLine1(faker.address().streetAddress())
                    .withAddressLine2(faker.address().secondaryAddress())
                    .withStreetName(faker.address().streetAddress())
                    .withPostCode(faker.address().zipCode())
                    .withTown(faker.address().city())
                    .withCountry(faker.address().countryCode())
                    .withCountrySubDivision(faker.address().state())));
        }

        return new ExternalContact()
            .withExternalId(UUID.randomUUID().toString().substring(0, 32))
            .withName(faker.name().fullName())
            .withAlias(faker.lorem().characters(10))
            .withContactPerson(faker.name().fullName())
            .withEmailId(faker.internet().emailAddress())
            .withPhoneNumber(faker.phoneNumber().phoneNumber())
            .withCategory(faker.lorem().characters(10))
            .withAddress(new Address()
                .withAddressLine1(faker.address().streetAddress())
                .withAddressLine2(faker.address().secondaryAddress())
                .withStreetName(faker.address().streetAddress())
                .withPostCode(faker.address().zipCode())
                .withTown(faker.address().city())
                .withCountry(faker.address().countryCode())
                .withCountrySubDivision(faker.address().state()))
            .withAccounts(accounts);
    }
}
