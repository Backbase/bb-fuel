package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.BBAN_ACCOUNT_TYPE;
import static com.backbase.ct.bbfuel.data.CommonConstants.IBAN_ACCOUNT_TYPE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_ACCOUNT_TYPES;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateRandomIban;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
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

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
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
        List<String> availableAccountTypes = globalProperties.getList(PROPERTY_CONTACTS_ACCOUNT_TYPES);

        for (int i = 0; i < numberOfAccounts; i++) {
            ExternalAccountInformation externalAccountInformation = new ExternalAccountInformation()
                .withExternalId(UUID.randomUUID().toString().substring(0, 32))
                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
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
                    .withCountrySubDivision(faker.address().state()));

            if (availableAccountTypes.contains(IBAN_ACCOUNT_TYPE)) {
                externalAccountInformation = externalAccountInformation.withIban(generateRandomIban());
            }

            if (availableAccountTypes.contains(BBAN_ACCOUNT_TYPE)) {
                int randomBbanAccount = CommonHelpers.generateRandomNumberInRange(100000, 999999999);
                externalAccountInformation = externalAccountInformation.withAccountNumber(String.valueOf(randomBbanAccount));
            }

            accounts.add(externalAccountInformation);
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
