package com.backbase.testing.dataloader.data;

import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.AccountIdentification;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.Identification;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.IdentifiedPaymentOrder;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiateCreditTransaction;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InvolvedParty;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.PostalAddress;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.Schedule;
import com.backbase.rest.spec.common.types.Currency;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.github.javafaker.Faker;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class PaymentsDataGenerator {

    private Faker faker = new Faker();
    private Random random = new Random();
    private ProductSummaryDataGenerator productSummaryDataGenerator = new ProductSummaryDataGenerator();

    public InitiatePaymentOrder generateInitiatePaymentOrder(String debtorArrangementId) {
        IdentifiedPaymentOrder.PaymentMode paymentMode = IdentifiedPaymentOrder.PaymentMode.values()[random.nextInt(IdentifiedPaymentOrder.PaymentMode.values().length)];
        Schedule schedule = null;

        if (paymentMode == IdentifiedPaymentOrder.PaymentMode.RECURRING) {
            schedule = new Schedule()
                    .withStartDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime()))
                    .withEvery(Schedule.Every.values()[random.nextInt(Schedule.Every.values().length)])
                    .withNonWorkingDayExecutionStrategy(Schedule.NonWorkingDayExecutionStrategy.values()[random.nextInt(Schedule.NonWorkingDayExecutionStrategy.values().length)])
                    .withTransferFrequency(Schedule.TransferFrequency.values()[random.nextInt(Schedule.TransferFrequency.values().length)])
                    .withOn(CommonHelpers.generateRandomNumberInRange(1, 7))
                    .withEndDate(new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addYears(new Date(), 1)));
        }

        return new InitiatePaymentOrder()
                .withDebtorAccount(new AccountIdentification()
                        .withName(faker.lorem().sentence(3, 0).replace(".", ""))
                        .withIdentification(new Identification()
                                .withSchemeName(Identification.SchemeName.ID)
                                .withIdentification(debtorArrangementId)))
                .withBatchBooking(false)
                .withInstructionPriority(IdentifiedPaymentOrder.InstructionPriority.values()[random.nextInt(IdentifiedPaymentOrder.InstructionPriority.values().length)])
                .withPaymentMode(paymentMode)
                .withRequestedExecutionDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime()))
                .withSchedule(schedule)
                .withCreditTransferTransactionInformation(Collections.singletonList(new InitiateCreditTransaction()
                        .withEndToEndIdentification(faker.lorem().characters(10))
                        .withCreditorAccount(new AccountIdentification()
                                .withIdentification(new Identification()
                                        .withSchemeName(Identification.SchemeName.IBAN)
                                        .withIdentification(productSummaryDataGenerator.generateRandomIban())))
                        .withInstructedAmount(new Currency()
                            .withCurrencyCode("EUR")
                            .withAmount(CommonHelpers.generateRandomAmountInRange(1000L, 99999L)))
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
                            .withCountrySubDivision(faker.address().state())))));
    }
}
