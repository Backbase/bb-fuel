package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_CONTACTS_ACCOUNT_TYPES;
import static com.backbase.ct.bbfuel.data.ProductSummaryDataGenerator.generateRandomIban;
import static com.backbase.ct.bbfuel.util.CommonHelpers.createRandomValidRtn;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.AccessContext;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.AccessContextScope;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.Address;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ContactsBulkPostRequestBody;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ExternalAccountInformation;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ExternalContact;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContactsDataGenerator {

    private ContactsDataGenerator() {
    }

    private static final GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final Faker faker = new Faker();
    private static final List<String> VALID_BIC_LIST = asList("ABNANL2A", "ANDLNL2A", "ARBNNL22", "ARSNNL21");
    private static final List<String> VALID_ACCOUNT_TYPE = asList("CHECKING", "SAVINGS");

    public static ContactsBulkPostRequestBody generateContactsBulkIngestionPostRequestBody(
        String externalServiceAgreementId, String externalUserId, int numberOfContacts,
        int numberOfAccountsPerContact) {
        return new ContactsBulkPostRequestBody()
            .accessContext(new AccessContext()
                .externalServiceAgreementId(externalServiceAgreementId)
                .externalLegalEntityId(null)
                .externalUserId(externalUserId)
                .scope(AccessContextScope.SA))
            .contacts(generateContacts(numberOfContacts, numberOfAccountsPerContact));
    }

    private static List<ExternalContact> generateContacts(int numberOfContacts, int numberOfAccountsPerContact) {
        return IntStream.range(0, numberOfContacts)
            .mapToObj(i -> generateContact(numberOfAccountsPerContact)).collect(Collectors.toList());
    }

    private static ExternalContact generateContact(int numberOfAccounts) {
        List<ExternalAccountInformation> accounts = new ArrayList<>();

        for (int i = 0; i < numberOfAccounts; i++) {
            ExternalAccountInformation externalAccountInformation = new ExternalAccountInformation()
                .externalId(UUID.randomUUID().toString().substring(0, 32))
                .name(faker.lorem().sentence(3, 0).replace(".", ""))
                .alias(faker.lorem().characters(10))
                .bic(VALID_BIC_LIST.get(faker.random().nextInt(VALID_BIC_LIST.size())))
                .accountHolderAddress(new Address()
                    .addressLine1(faker.address().streetAddress())
                    .addressLine2(faker.address().secondaryAddress())
                    .streetName(faker.address().streetAddress())
                    .postCode(faker.address().zipCode())
                    .town(faker.address().cityName())
                    .country(faker.address().countryCode())
                    .countrySubDivision(faker.address().state()))
                .accountType(VALID_ACCOUNT_TYPE.get(faker.random().nextInt(VALID_ACCOUNT_TYPE.size())))
                .bankCode(createRandomValidRtn())
                .bankAddress(new Address()
                    .addressLine1(faker.address().streetAddress())
                    .addressLine2(faker.address().secondaryAddress())
                    .streetName(faker.address().streetAddress())
                    .postCode(faker.address().zipCode())
                    .town(faker.address().city())
                    .country(faker.address().countryCode())
                    .countrySubDivision(faker.address().state()));

            externalAccountInformation = determineTheUseOfIBANorBBAN(externalAccountInformation);

            accounts.add(externalAccountInformation);
        }

        return new ExternalContact()
            .externalId(UUID.randomUUID().toString().substring(0, 32))
            .name(faker.name().fullName())
            .alias(faker.lorem().characters(10))
            .contactPerson(faker.name().fullName())
            .emailId(faker.internet().emailAddress())
            .phoneNumber(faker.phoneNumber().phoneNumber())
            .category(faker.lorem().characters(10))
            .address(new Address()
                .addressLine1(faker.address().streetAddress())
                .addressLine2(faker.address().secondaryAddress())
                .streetName(faker.address().streetAddress())
                .postCode(faker.address().zipCode())
                .town(faker.address().city())
                .country(faker.address().countryCode())
                .countrySubDivision(faker.address().state()))
            .accounts(accounts);
    }

    private static ExternalAccountInformation determineTheUseOfIBANorBBAN(
        ExternalAccountInformation externalAccountInformation) {
        final String BBAN = "BBAN";
        final String IBAN = "IBAN";
        String availableAccountType = globalProperties.getString(PROPERTY_CONTACTS_ACCOUNT_TYPES);
        ExternalAccountInformation returnedExternalAccountInformation;

        switch (availableAccountType.toUpperCase()) {
            case IBAN:
                returnedExternalAccountInformation = externalAccountInformation.iban(generateRandomIban());
                break;

            case BBAN:
                int randomBbanAccount = CommonHelpers.generateRandomNumberInRange(100000, 999999999);
                returnedExternalAccountInformation = externalAccountInformation.accountNumber(
                    String.valueOf(randomBbanAccount));
                break;

            default:
                throw new IllegalStateException(
                    "Unexpected value: " + availableAccountType + ". Please use IBAN or BBAN");
        }

        return returnedExternalAccountInformation;
    }
}
