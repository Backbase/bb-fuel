package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_ACCOUNT_TYPES;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ExternalAccountInformation;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ExternalContact;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.contacts.AccessContext;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.contacts.Address;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.contacts.ContactsBulkIngestionPostRequestBody;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.groovy.util.Maps;

public class ContactsDataGenerator {

    private ContactsDataGenerator() {
    }

    private static final GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final Faker faker = new Faker();
    private static final List<String> VALID_BIC_LIST = asList("ABNANL2A", "ANDLNL2A", "ARBNNL22", "ARSNNL21");
    private static final Map<String, String> VALID_COUNTRY_TO_IBAN = Maps.of(
        "AE", "AE460090000000123456789",
        "BR", "BR1500000000000010932840814P2",
        "GB", "GB98MIDL07009312345678",
        "JO", "JO71CBJO0000000000001234567890",
        "NL", "NL02ABNA0123456789"
    );

    public static ContactsBulkIngestionPostRequestBody generateContactsBulkIngestionPostRequestBody(
        String externalServiceAgreementId, String externalUserId, int numberOfContacts,
        int numberOfAccountsPerContact) {
        return new ContactsBulkIngestionPostRequestBody()
            .withAccessContext(new AccessContext()
                .withExternalServiceAgreementId(externalServiceAgreementId)
                .withExternalLegalEntityId(null)
                .withExternalUserId(externalUserId)
                .withScope(AccessContext.Scope.SA))
            .withContacts(generateContacts(numberOfContacts, numberOfAccountsPerContact));
    }

    private static List<ExternalContact> generateContacts(int numberOfContacts, int numberOfAccountsPerContact) {
        return IntStream.range(0, numberOfContacts)
            .mapToObj(i -> generateContact(numberOfAccountsPerContact)).collect(Collectors.toList());
    }

    private static ExternalContact generateContact(int numberOfAccounts) {
        Random random = new Random();
        List<ExternalAccountInformation> accounts = new ArrayList<>();

        for (int i = 0; i < numberOfAccounts; i++) {
            ExternalAccountInformation externalAccountInformation = new ExternalAccountInformation()
                .withExternalId(UUID.randomUUID().toString().substring(0, 32))
                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                .withAlias(faker.lorem().characters(10))
                .withBic(getRandomBic())
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
                    .withCountry(getRandomCountry())
                    .withCountrySubDivision(faker.address().state()));

            externalAccountInformation = determineTheUseOfIBANorBBAN(externalAccountInformation);

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

    private static ExternalAccountInformation determineTheUseOfIBANorBBAN(
        ExternalAccountInformation externalAccountInformation) {
        final String BBAN = "BBAN";
        final String IBAN = "IBAN";
        String availableAccountType = globalProperties.getString(PROPERTY_CONTACTS_ACCOUNT_TYPES);
        ExternalAccountInformation returnedExternalAccountInformation;

        switch (availableAccountType.toUpperCase()) {
            case IBAN:
                returnedExternalAccountInformation = externalAccountInformation.withIban(
                    getIbanForCountry(externalAccountInformation.getBankAddress().getCountry()));
                break;

            case BBAN:
                int randomBbanAccount = CommonHelpers.generateRandomNumberInRange(100000, 999999999);
                returnedExternalAccountInformation = externalAccountInformation.withAccountNumber(
                    String.valueOf(randomBbanAccount));
                break;

            default:
                throw new IllegalStateException(
                    "Unexpected value: " + availableAccountType + ". Please use IBAN or BBAN");
        }

        return returnedExternalAccountInformation;
    }

    private static String getRandomCountry() {
        List<String> countries = new ArrayList<>(VALID_COUNTRY_TO_IBAN.keySet());
        return countries.get(new Random().nextInt(countries.size()));
    }

    private static String getIbanForCountry(String country) {
        if (!VALID_COUNTRY_TO_IBAN.containsKey(country)) {
            throw new IllegalArgumentException(String.format("Country %s is not supported", country));
        }
        return VALID_COUNTRY_TO_IBAN.get(country);
    }

    private static String getRandomBic() {
        return VALID_BIC_LIST.get(new Random().nextInt(VALID_BIC_LIST.size()));
    }
}
