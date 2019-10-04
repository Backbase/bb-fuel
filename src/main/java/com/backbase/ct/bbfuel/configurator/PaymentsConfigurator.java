package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_ACH_DEBIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_SEPA_CREDIT_TRANSFER;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_DOMESTIC_WIRE;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final PaymentOrderPresentationRestClient paymentOrderPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private Random random = new Random();

    public void ingestPaymentOrders(String externalUserId) {
        final List<String> PAYMENT_TYPES = Arrays
            .asList(PAYMENT_TYPE_SEPA_CREDIT_TRANSFER, PAYMENT_TYPE_US_DOMESTIC_WIRE, PAYMENT_TYPE_ACH_DEBIT);

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        List<ArrangementsByBusinessFunctionGetResponseBody> sepaCtArrangements = productSummaryPresentationRestClient
            .getSepaCtArrangements();
        List<ArrangementsByBusinessFunctionGetResponseBody> usDomesticWireArrangements = productSummaryPresentationRestClient
            .getUsDomesticWireArrangements();
        List<ArrangementsByBusinessFunctionGetResponseBody> achDebitArrangements = productSummaryPresentationRestClient
            .getAchDebitArrangements();

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MAX));

        if (!sepaCtArrangements.isEmpty() && !usDomesticWireArrangements.isEmpty()
            && !achDebitArrangements.isEmpty()) {

            IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
                String paymentType = getRandomFromList(PAYMENT_TYPES);
                ArrangementsByBusinessFunctionGetResponseBody randomArrangement;

                if (PAYMENT_TYPE_SEPA_CREDIT_TRANSFER.equals(paymentType)) {
                    randomArrangement = getRandomFromList(sepaCtArrangements);
                } else if (PAYMENT_TYPE_ACH_DEBIT.equals(paymentType)) {
                    randomArrangement = getRandomFromList(achDebitArrangements);
                } else {
                    randomArrangement = getRandomFromList(usDomesticWireArrangements);
                }

                InitiatePaymentOrder initiatePaymentOrder = PaymentsDataGenerator
                    .generateInitiatePaymentOrder(randomArrangement.getId(), randomArrangement.getCurrency(), paymentType);
                paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
                    .then()
                    .statusCode(SC_ACCEPTED);

                log.info("Payment order ingested for debtor account [{}] for user [{}]",
                    initiatePaymentOrder.getDebtorAccount().getIdentification().getIdentification(), externalUserId);
            });
        }
    }
}
