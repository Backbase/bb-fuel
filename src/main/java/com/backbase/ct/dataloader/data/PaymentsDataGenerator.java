package com.backbase.ct.dataloader.data;

import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.AccountIdentification;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.Bank;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.Identification;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.IdentifiedPaymentOrder;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiateCreditTransaction;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiateCreditorAccount;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InvolvedParty;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.PostalAddress;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.Schedule;
import com.backbase.rest.spec.common.types.Currency;
import com.github.javafaker.Faker;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.backbase.ct.dataloader.data.CommonConstants.PAYMENT_TYPE_SEPA_CREDIT_TRANSFER;

public class PaymentsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();
    private static List<String> branchCodes = Arrays.asList("114923756", "114910222", "124000054", "113011258", "113110586", "121002042", "122003396", "122232109", "122237625", "122237997", "122238572", "122105045", "122105171", "122105320", "122400779", "123006965", "125008013", "125108489", "226072870", "265270002", "253278058", "253271806", "242277675", "071993162", "091512251", "075911713", "071001122", "231278274", "272485673", "291479178", "255075576", "311376494", "241078875", "244183631", "244077129", "241076097", "244273826", "044204370", "243278534", "242086361", "241273188", "244077815", "241075153", "073911870", "303184610", "303986151", "263277887", "103101848", "103101013", "303986096");

    public static InitiatePaymentOrder generateInitiatePaymentOrder(String debtorArrangementId, String paymentType) {
        IdentifiedPaymentOrder.PaymentMode paymentMode = IdentifiedPaymentOrder.PaymentMode.values()[random.nextInt(IdentifiedPaymentOrder.PaymentMode.values().length)];
        Schedule schedule = null;
        Bank creditorBank = null;
        Bank correspondentBank = null;
        Currency currency = new Currency().withAmount(CommonHelpers.generateRandomAmountInRange(1000L, 99999L));
        Identification identification;

        if (paymentMode.equals(IdentifiedPaymentOrder.PaymentMode.RECURRING)) {
            schedule = new Schedule()
                    .withStartDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                    .withEvery(Schedule.Every.values()[random.nextInt(Schedule.Every.values().length)])
                    .withNonWorkingDayExecutionStrategy(Schedule.NonWorkingDayExecutionStrategy.values()[random.nextInt(Schedule.NonWorkingDayExecutionStrategy.values().length)])
                    .withTransferFrequency(Schedule.TransferFrequency.values()[random.nextInt(Schedule.TransferFrequency.values().length)])
                    .withOn(CommonHelpers.generateRandomNumberInRange(1, 7))
                    .withEndDate(new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addYears(new Date(), 1)));
        }

        if (PAYMENT_TYPE_SEPA_CREDIT_TRANSFER.equals(paymentType)) {
            currency.setCurrencyCode("EUR");
            identification = generateIbanIdentification();
        } else {
            creditorBank = generateCreditorBank();
            correspondentBank = generateCorrespondentBank();
            currency.setCurrencyCode("USD");
            identification = generateBbanIdentification();
        }

        return new InitiatePaymentOrder()
                .withDebtorAccount(new AccountIdentification()
                        .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                        .withIdentification(new Identification().withSchemeName(Identification.SchemeName.ID).withIdentification(debtorArrangementId)))
                .withBatchBooking(false)
                .withInstructionPriority(IdentifiedPaymentOrder.InstructionPriority.values()[random.nextInt(IdentifiedPaymentOrder.InstructionPriority.values().length)])
                .withPaymentMode(paymentMode)
                .withPaymentType(paymentType)
                .withRequestedExecutionDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .withSchedule(schedule)
                .withCreditTransferTransactionInformation(Collections.singletonList(new InitiateCreditTransaction()
                        .withEndToEndIdentification(faker.lorem().characters(10))
                        .withCreditorAccount(new InitiateCreditorAccount()
                                .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                                .withIdentification(identification))
                        .withInstructedAmount(currency)
                        .withRemittanceInformation(faker.lorem().sentence(3, 0).replace(".", ""))
                        .withCreditor(new InvolvedParty()
                                .withName(faker.name().fullName())
                                .withPostalAddress(new PostalAddress()
                                        .withAddressLine1(faker.address().streetAddress())
                                        .withAddressLine2(faker.address().secondaryAddress())
                                        .withStreetName(faker.address().streetAddress())
                                        .withPostCode(faker.address().zipCode())
                                        .withTown(faker.address().city())
                                        .withCountry(faker.address().countryCode())
                                        .withCountrySubDivision(faker.address().state())))
                        .withCreditorBank(creditorBank)
                        .withCorrespondentBank(correspondentBank)));
    }

    private static Identification generateIbanIdentification() {
        return new Identification()
            .withSchemeName(Identification.SchemeName.IBAN)
            .withIdentification(ProductSummaryDataGenerator.generateRandomIban());
    }

    private static Identification generateBbanIdentification() {
        return new Identification()
                .withSchemeName(Identification.SchemeName.BBAN)
                .withIdentification(String.valueOf(CommonHelpers.generateRandomNumberInRange(0, 999999999)));
    }

    private static Bank generateCorrespondentBank() {
        return new Bank()
                .withBankBranchCode(branchCodes.get(random.nextInt(branchCodes.size())))
                .withName(faker.name().fullName());
    }

    private static Bank generateCreditorBank() {
        return new Bank()
                .withBankBranchCode(branchCodes.get(random.nextInt(branchCodes.size())))
                .withName(faker.name().fullName())
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