package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_ACH_DEBIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_SEPA_CREDIT_TRANSFER;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_DOMESTIC_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_FOREIGN_WIRE;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.payment.PaymentOrderPresentationRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.PaymentsDataGenerator;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final PaymentOrderPresentationRestClient paymentOrderPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private Random random = new Random();

    public void ingestPaymentOrders(String externalUserId) {
        final List<String> PAYMENT_TYPES = Arrays
            .asList(PAYMENT_TYPE_SEPA_CREDIT_TRANSFER, PAYMENT_TYPE_US_DOMESTIC_WIRE, PAYMENT_TYPE_ACH_DEBIT, PAYMENT_TYPE_US_FOREIGN_WIRE);

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        List<ArrangementsByBusinessFunctionGetResponseBody> sepaCtArrangements = productSummaryPresentationRestClient
            .getSepaCtArrangements();
        List<ArrangementsByBusinessFunctionGetResponseBody> usDomesticWireArrangements = productSummaryPresentationRestClient
            .getUsDomesticWireArrangements();
        List<ArrangementsByBusinessFunctionGetResponseBody> achDebitArrangements = productSummaryPresentationRestClient
            .getAchDebitArrangements();
        List<ArrangementsByBusinessFunctionGetResponseBody> usForeignWireArrangements = productSummaryPresentationRestClient
            .getUsForeignWireArrangements();

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            String paymentType = getRandomFromList(PAYMENT_TYPES);
            ArrangementsByBusinessFunctionGetResponseBody randomArrangement;

            if (PAYMENT_TYPE_SEPA_CREDIT_TRANSFER.equals(paymentType)) {
                randomArrangement = getRandomFromList(sepaCtArrangements);
            } else if (PAYMENT_TYPE_US_DOMESTIC_WIRE.equals(paymentType)) {
                randomArrangement = getRandomFromList(usDomesticWireArrangements);
            } else if (PAYMENT_TYPE_US_FOREIGN_WIRE.equals(paymentType)){
                randomArrangement = getRandomFromList(usForeignWireArrangements);
            } else if (PAYMENT_TYPE_ACH_DEBIT.equals(paymentType)){
                randomArrangement = getRandomFromList(achDebitArrangements);
            } else {
                throw new IllegalArgumentException("Unknown payment type " + paymentType);
            }

            InitiatePaymentOrder initiatePaymentOrder = PaymentsDataGenerator
                .generateInitiatePaymentOrder(randomArrangement.getId(), paymentType);
            paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
                .then()
                .statusCode(SC_ACCEPTED);

            LOGGER.info("Payment order ingested for debtor account [{}] for user [{}]",
                initiatePaymentOrder.getDebtorAccount().getIdentification().getIdentification(), externalUserId);
        });
    }
}
