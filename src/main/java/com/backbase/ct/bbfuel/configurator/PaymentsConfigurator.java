package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.payment.PaymentOrderPresentationRestClient;
import com.backbase.ct.bbfuel.client.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.PaymentsDataGenerator;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.paymentorder.client.api.v3.model.InitiatePaymentOrderWithId;
import com.backbase.dbs.arrangement.client.api.v2.model.ProductSummaryItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_ACH_DEBIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_SEPA_CREDIT_TRANSFER;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_DOMESTIC_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_FOREIGN_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_PAYMENTS_OOTB_TYPES;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final PaymentOrderPresentationRestClient paymentOrderPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final ProductSummaryPresentationRestClient productSummaryPresentationRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;

    public void ingestPaymentOrders(String externalUserId) {

        List<String> ootbPaymentTypes = globalProperties.getList(PROPERTY_PAYMENTS_OOTB_TYPES);

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        List<ProductSummaryItem> sepaCtArrangements = productSummaryPresentationRestClient
            .getSepaCtArrangements();
        List<ProductSummaryItem> usDomesticWireArrangements = productSummaryPresentationRestClient
            .getUsDomesticWireArrangements();
        List<ProductSummaryItem> achDebitArrangements = productSummaryPresentationRestClient
            .getAchDebitArrangements();
        List<com.backbase.dbs.arrangement.client.api.v2.model.ProductSummaryItem> usForeignWireArrangements = productSummaryPresentationRestClient
            .getUSForeignWireArrangements();

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MAX));

        if (!isEmpty(sepaCtArrangements) && !isEmpty(usDomesticWireArrangements)
            && !isEmpty(achDebitArrangements) && !isEmpty(usForeignWireArrangements)) {

            IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
                String paymentType = getRandomFromList(ootbPaymentTypes);
                ProductSummaryItem randomArrangement;

                if (PAYMENT_TYPE_SEPA_CREDIT_TRANSFER.equals(paymentType)) {
                    randomArrangement = getRandomFromList(sepaCtArrangements);
                } else if (PAYMENT_TYPE_ACH_DEBIT.equals(paymentType)) {
                    randomArrangement = getRandomFromList(achDebitArrangements);
                } else if (PAYMENT_TYPE_US_FOREIGN_WIRE.equals(paymentType)) {
                    randomArrangement = getRandomFromList(usForeignWireArrangements);
                } else if (PAYMENT_TYPE_US_DOMESTIC_WIRE.equals(paymentType)) {
                    randomArrangement = getRandomFromList(usDomesticWireArrangements);
                } else {
                    throw new IllegalArgumentException("Unknown payment type " + paymentType);
                }

                InitiatePaymentOrderWithId initiatePaymentOrder = PaymentsDataGenerator
                    .generateInitiatePaymentOrder(randomArrangement.getId(), randomArrangement.getCurrency(), paymentType);
                paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
                    .then()
                    .statusCode(SC_ACCEPTED);

                log.info("Payment order ingested for originator account [{}] for user [{}]",
                    initiatePaymentOrder.getOriginatorAccount().getIdentification().getIdentification(), externalUserId);
            });
        }
    }
}
