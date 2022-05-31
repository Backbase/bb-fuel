package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_ACCOUNT_TYPES;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_SANCTIONED_COUNTRIES;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ExternalAccountInformation;
import com.backbase.dbs.integration.external.inbound.contact.rest.spec.v2.contacts.ExternalContact;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.contacts.AccessContext;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.contacts.Address;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.contacts.ContactsBulkIngestionPostRequestBody;
import com.github.javafaker.Faker;
import com.github.jknack.handlebars.internal.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.iban4j.bban.BbanStructure;

public class ContactsDataGenerator {

    private ContactsDataGenerator() {
    }

    private static final GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final Faker faker = new Faker();
    private static final List<String> SUPPORTED_SANCTIONED_COUNTRIES =
        asList("AE", "AU", "BR", "CA", "CN", "GB", "HK", "IN", "JO", "JP", "NL", "RU", "SG", "US", "ZA");
    private static final List<String> SUPPORTED_CONTACTS_COUNTRIES =
        asList("AR", "AU", "AT", "BE", "BR", "CA", "CN", "CO", "HR", "CZ", "DK", "EC", "EG", "ET", "FR",
            "DE", "GR", "HN", "HK", "HU", "IN", "ID", "IE", "IT", "JM", "MY", "MX", "MA", "NA", "NL", "NZ",
            "NO", "PL", "PT", "RU", "SN", "SG", "ZA", "ES", "LK", "SE", "TH", "TR", "UG", "AE", "GB", "US", "ZW");
    private static final List<String> SUPPORTED_COUNTRIES = getSupportedCountries();

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
        List<ExternalAccountInformation> accounts = new ArrayList<>();

        for (int i = 0; i < numberOfAccounts; i++) {
            String country = getRandomCountry();
            ExternalAccountInformation externalAccountInformation = new ExternalAccountInformation()
                .withExternalId(UUID.randomUUID().toString().substring(0, 32))
                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                .withAlias(faker.lorem().characters(10))
                .withBic(getBicForCountry(country))
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
                    .withCountry(country)
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
        ExternalAccountInformation returnedExternalAccountInformation;

        if (useIbanAccounts()) {
            returnedExternalAccountInformation = externalAccountInformation.withIban(
                getIbanForCountry(externalAccountInformation.getBankAddress().getCountry()));
        } else {
            returnedExternalAccountInformation = externalAccountInformation.withAccountNumber(
                getBbanForCountry(externalAccountInformation.getBankAddress().getCountry()));
        }

        return returnedExternalAccountInformation;
    }

    private static String getRandomCountry() {
        return SUPPORTED_COUNTRIES.get(new Random().nextInt(SUPPORTED_COUNTRIES.size()));
    }

    private static String getIbanForCountry(String country) {
        return Iban.random(CountryCode.getByCode(country)).toString();
    }

    private static String getBbanForCountry(String country) {
        return Iban.random(CountryCode.getByCode(country)).getBban();
    }

    private static String getBicForCountry(String country) {
        return Iban.random(CountryCode.getByCode(country)).getBankCode();
    }

    private static List<String> getSupportedCountries() {
        return globalProperties.getBoolean(PROPERTY_CONTACTS_SANCTIONED_COUNTRIES)
            ? applyCountryFilter(SUPPORTED_SANCTIONED_COUNTRIES)
            : applyCountryFilter(SUPPORTED_CONTACTS_COUNTRIES);
    }

    private static List<String> applyCountryFilter(List<String> countries) {
        // filtering not supported countries by iban4j library
        Set<String> supportedCountryCodes = BbanStructure.supportedCountries().stream()
            .map(CountryCode::getAlpha2)
            .collect(Collectors.toSet());
        return countries.stream()
            .filter(supportedCountryCodes::contains)
            .collect(Collectors.toList());
    }

    private static boolean useIbanAccounts() {
        String availableAccountType = globalProperties.getString(PROPERTY_CONTACTS_ACCOUNT_TYPES);
        if (StringUtils.isBlank(availableAccountType)) {
            throw new IllegalStateException(
                "Unexpected value: " + availableAccountType + ". Please use IBAN or BBAN");
        }

        return "iban".equalsIgnoreCase(availableAccountType);
    }
}
