package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_ACH_DEBIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_SEPA_CREDIT_TRANSFER;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.dbs.paymentorder.client.api.v3.model.AccountIdentification;
import com.backbase.dbs.paymentorder.client.api.v3.model.Bank;
import com.backbase.dbs.paymentorder.client.api.v3.model.CurrencyTyped;
import com.backbase.dbs.paymentorder.client.api.v3.model.Identification;
import com.backbase.dbs.paymentorder.client.api.v3.model.InitiateCounterpartyAccount;
import com.backbase.dbs.paymentorder.client.api.v3.model.InitiatePaymentOrderWithId;
import com.backbase.dbs.paymentorder.client.api.v3.model.InitiateTransaction;
import com.backbase.dbs.paymentorder.client.api.v3.model.InstructionPriority;
import com.backbase.dbs.paymentorder.client.api.v3.model.InvolvedParty;
import com.backbase.dbs.paymentorder.client.api.v3.model.PaymentMode;
import com.backbase.dbs.paymentorder.client.api.v3.model.PostalAddress;
import com.backbase.dbs.paymentorder.client.api.v3.model.Schedule;
import com.backbase.dbs.paymentorder.client.api.v3.model.Schedule.EveryEnum;
import com.backbase.dbs.paymentorder.client.api.v3.model.Schedule.NonWorkingDayExecutionStrategyEnum;
import com.backbase.dbs.paymentorder.client.api.v3.model.Schedule.TransferFrequencyEnum;
import com.backbase.dbs.paymentorder.client.api.v3.model.SchemeNames;
import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PaymentsDataGenerator {

    private static Faker faker = new Faker();
    private static List<String> branchCodes = Arrays
        .asList("114923756", "114910222", "124000054", "113011258", "113110586", "121002042", "122003396", "122232109",
            "122237625", "122237997", "122238572", "122105045", "122105171", "122105320", "122400779", "123006965",
            "125008013", "125108489", "226072870", "265270002", "253278058", "253271806", "242277675", "071993162",
            "091512251", "075911713", "071001122", "231278274", "272485673", "291479178", "255075576", "311376494",
            "241078875", "244183631", "244077129", "241076097", "244273826", "044204370", "243278534", "242086361",
            "241273188", "244077815", "241075153", "073911870", "303184610", "303986151", "263277887", "103101848",
            "103101013", "303986096");
    private static List<String> bicCodes = Arrays
        .asList("CHASUS33XXX", "BOFAUS3NXXX", "MIDLGB22XXX", "BARCGB22XXX", "ABNANL2AXXX", "CITIUS33XXX", "WFBIUS6SXXX",
            "NWBKGB2LXXX", "COBADEFFXXX", "BNPAFRPPXXX", "POALILITXXX", "LOYDGB2LXXX", "NTSBDEB1XXX", "DEUTDEDBPAL",
            "AXISINBB002");

    public static InitiatePaymentOrderWithId generateInitiatePaymentOrder(String originatorArrangementId,
        String originatorArrangementCurrency, String paymentType) {
        PaymentMode paymentMode = getRandomFromEnumValues(PaymentMode.values());
        Schedule schedule = null;
        Bank counterpartyBank = null;
        Bank correspondentBank = null;
        CurrencyTyped currency = new CurrencyTyped();
        currency.setAmount(CommonHelpers.generateRandomAmountInRange(1000L, 99999L));
        Identification identification;

        if (paymentMode == PaymentMode.RECURRING) {
            schedule = new Schedule()
                .withStartDate(LocalDate.now())
                .withEvery(getRandomFromEnumValues(EveryEnum.values()))
                .withNonWorkingDayExecutionStrategy(
                    getRandomFromEnumValues(NonWorkingDayExecutionStrategyEnum.values()))
                .withTransferFrequency(getRandomFromEnumValues(TransferFrequencyEnum.values()))
                .withOn(CommonHelpers.generateRandomNumberInRange(1, 7))
                .withEndDate(LocalDate.now().plusYears(1L));
        }

        if (PAYMENT_TYPE_SEPA_CREDIT_TRANSFER.equals(paymentType)) {
            currency.setCurrencyCode("EUR");
            identification = generateIbanIdentification();
        } else if (PAYMENT_TYPE_ACH_DEBIT.equals(paymentType)) {
            counterpartyBank = generateCounterpartyBank();
            currency.setCurrencyCode(originatorArrangementCurrency);
            identification = generateBbanIdentification();
        } else {
            counterpartyBank = generateCounterpartyBank();
            correspondentBank = generateCorrespondentBank();
            currency.setCurrencyCode("USD");
            identification = generateBbanIdentification();
        }

        InitiateCounterpartyAccount initiateCounterpartyAccount = new InitiateCounterpartyAccount();
        initiateCounterpartyAccount.setName(faker.lorem().sentence(3, 0).replace(".", ""));
        initiateCounterpartyAccount.setIdentification(identification);

        InitiatePaymentOrderWithId initiatePaymentOrder = new InitiatePaymentOrderWithId();
        initiatePaymentOrder
            .withId(UUID.randomUUID().toString())
            .withOriginatorAccount(new AccountIdentification()
                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                .withIdentification(new Identification().withSchemeName(SchemeNames.ID)
                    .withIdentification(originatorArrangementId)))
            .withInstructionPriority(getRandomFromEnumValues(InstructionPriority.values()))
            .withPaymentMode(paymentMode)
            .withPaymentType(paymentType)
            .withRequestedExecutionDate(LocalDate.now())
            .withSchedule(schedule)
            .withTransferTransactionInformation(new InitiateTransaction()
                .withEndToEndIdentification(faker.lorem().characters(10))
                .withCounterpartyAccount(initiateCounterpartyAccount)
                .withInstructedAmount(currency)
                .withRemittanceInformation(faker.lorem().sentence(3, 0).replace(".", ""))
                .withCounterparty(new InvolvedParty()
                    .withName(faker.name().fullName())
                    .withPostalAddress(new PostalAddress()
                        .withAddressLine1(faker.address().streetAddress())
                        .withAddressLine2(faker.address().secondaryAddress())
                        .withStreetName(faker.address().streetAddress())
                        .withPostCode(faker.address().zipCode())
                        .withTown(faker.address().city())
                        .withCountry(faker.address().countryCode())
                        .withCountrySubDivision(faker.address().state())))
                .withCounterpartyBank(counterpartyBank)
                .withCorrespondentBank(correspondentBank));
        return initiatePaymentOrder;
    }

    private static Identification generateIbanIdentification() {
        return new Identification()
            .withSchemeName(SchemeNames.IBAN)
            .withIdentification(ProductSummaryDataGenerator.generateRandomIban());
    }

    private static Identification generateBbanIdentification() {
        return new Identification()
            .withSchemeName(SchemeNames.BBAN)
            .withIdentification(String.valueOf(CommonHelpers.generateRandomNumberInRange(0, 999999999)));
    }

    private static Bank generateCorrespondentBank() {
        return new Bank()
            .withBankBranchCode(getRandomFromList(branchCodes))
            .withName(faker.name().fullName());
    }

    private static Bank generateCounterpartyBank() {
        return new Bank()
            .withBankBranchCode(getRandomFromList(branchCodes))
            .withName(faker.name().fullName())
            .withBic(getRandomFromList(bicCodes))
            .withPostalAddress(new PostalAddress()
                .withAddressLine1(faker.address().streetAddress())
                .withAddressLine2(faker.address().secondaryAddress())
                .withStreetName(faker.address().streetAddress())
                .withPostCode(faker.address().zipCode())
                .withTown(faker.address().city())
                .withCountry(faker.address().countryCode())
                .withCountrySubDivision(faker.address().state()));
    }
}
