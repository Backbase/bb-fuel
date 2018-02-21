package com.backbase.testing.dataloader.data;

import com.backbase.presentation.contact.rest.spec.v2.contacts.AccountInformation;
import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsPostRequestBody;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONTACT_ACCOUNTS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONTACT_ACCOUNTS_MIN;
import static com.backbase.testing.dataloader.data.ProductSummaryDataGenerator.generateRandomIban;

public class ContactsDataGenerator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static Faker faker = new Faker();

    public static ContactsPostRequestBody generateContactsPostRequestBody() {
        List<AccountInformation> accounts = new ArrayList<>();

        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_CONTACT_ACCOUNTS_MIN), globalProperties.getInt(PROPERTY_CONTACT_ACCOUNTS_MAX)); i++) {
            accounts.add(new AccountInformation().withName(faker.lorem().sentence(3, 0).replace(".", ""))
                    .withIBAN(generateRandomIban())
                    .withAccountNumber(faker.finance().iban())
                    .withAlias(faker.lorem().characters(10))
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
                    .withBankCountrySubDivision(faker.address().state())
            );
        }

        return new ContactsPostRequestBody().withName(faker.name().fullName())
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
