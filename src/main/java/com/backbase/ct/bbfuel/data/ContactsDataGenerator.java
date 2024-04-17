package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.createRandomValidRtn;
import static java.util.Arrays.asList;
import static org.iban4j.CountryCode.NL;
import static org.iban4j.CountryCode.US;

import com.backbase.dbs.contact.integration.inbound.api.v2.model.AccessContext;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.AccessContextScope;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.Address;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ContactsBulkPostRequestBody;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ExternalAccountInformation;
import com.backbase.dbs.contact.integration.inbound.api.v2.model.ExternalContact;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class ContactsDataGenerator {

    private ContactsDataGenerator() {
    }

    private static final Faker faker = new Faker();
    private static final List<String> VALID_BIC_LIST = asList("ABNANL2A", "ANDLNL2A", "ARBNNL22", "ARSNNL21");
    private static final List<String> VALID_ACCOUNT_TYPE_LIST = asList("Checking", "Savings");

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
            ExternalAccountInformation externalAccountInformation = faker.random().nextInt(3) % 2 == 0 ?
                generateNlAccountInformation() :
                generateUsAccountInformation();
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
            .address(getAddress(null))
            .accounts(accounts);
    }

    private static ExternalAccountInformation generateNlAccountInformation() {
        return getBasicAccountInformation(NL)
            .iban(Iban.random(NL).toString());
    }

    private static ExternalAccountInformation generateUsAccountInformation() {
        return getBasicAccountInformation(US)
            .accountNumber(faker.number().digits(12));
    }

    private static ExternalAccountInformation getBasicAccountInformation(CountryCode code) {
        return new ExternalAccountInformation()
            .externalId(UUID.randomUUID().toString().substring(0, 32))
            .name(faker.lorem().sentence(3, 0).replace(".", ""))
            .alias(faker.lorem().characters(10))
            .bic(VALID_BIC_LIST.get(faker.random().nextInt(VALID_BIC_LIST.size())))
            .accountHolderAddress(getAddress(code))
            .accountType(VALID_ACCOUNT_TYPE_LIST.get(faker.random().nextInt(VALID_ACCOUNT_TYPE_LIST.size())))
            .bankCode(createRandomValidRtn())
            .bankAddress(getAddress(code));
    }

    private static com.backbase.dbs.contact.integration.inbound.api.v2.model.Address getAddress(CountryCode code) {
        return new Address()
            .addressLine1(faker.address().streetAddress())
            .addressLine2(faker.address().secondaryAddress())
            .streetName(faker.address().streetAddress())
            .postCode(faker.address().zipCode())
            .town(faker.address().city())
            .country(Objects.nonNull(code) ? code.toString() : faker.address().countryCode())
            .countrySubDivision(faker.address().state());
    }
}