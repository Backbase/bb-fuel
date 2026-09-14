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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_ACH_DEBIT;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_FCY_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_SEPA_CREDIT_TRANSFER;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_CROSS_BORDER_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_DOMESTIC_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_FOREIGN_WIRE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENT_TYPE_US_FX_FOREIGN_WIRE;
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

        Map<String, List<ProductSummaryItem>> arrangementsByPaymentType = new HashMap<>();
        for (String paymentType : ootbPaymentTypes) {
            List<ProductSummaryItem> arrangements = getArrangementsForPaymentType(paymentType);
            if (!isEmpty(arrangements)) {
                arrangementsByPaymentType.put(paymentType, arrangements);
            } else {
                log.warn("No debit arrangements available for configured payment type [{}]", paymentType);
            }
        }

        if (arrangementsByPaymentType.isEmpty()) {
            log.warn("Payment order ingestion skipped for user [{}]: no arrangements found for any configured type",
                externalUserId);
            return;
        }

        List<String> availablePaymentTypes = new ArrayList<>(arrangementsByPaymentType.keySet());

        for (String paymentType : availablePaymentTypes) {
            ingestPaymentOrder(externalUserId, paymentType, arrangementsByPaymentType.get(paymentType));
        }

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MAX));

        int additionalOrders = Math.max(0, randomAmount - availablePaymentTypes.size());
        IntStream.range(0, additionalOrders).parallel().forEach(randomNumber -> {
            String paymentType = getRandomFromList(availablePaymentTypes);
            ingestPaymentOrder(externalUserId, paymentType, arrangementsByPaymentType.get(paymentType));
        });
    }

    private void ingestPaymentOrder(String externalUserId, String paymentType,
        List<ProductSummaryItem> arrangements) {
        ProductSummaryItem randomArrangement = getRandomFromList(arrangements);

        InitiatePaymentOrderWithId initiatePaymentOrder = PaymentsDataGenerator
            .generateInitiatePaymentOrder(randomArrangement.getId(), randomArrangement.getCurrency(), paymentType);
        paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
            .then()
            .statusCode(SC_ACCEPTED);

        log.info("Payment order ingested for originator account [{}] for user [{}]",
            initiatePaymentOrder.getOriginatorAccount().getIdentification().getIdentification(), externalUserId);
    }

    private List<ProductSummaryItem> getArrangementsForPaymentType(String paymentType) {
        if (PAYMENT_TYPE_SEPA_CREDIT_TRANSFER.equals(paymentType)) {
            return productSummaryPresentationRestClient.getSepaCtArrangements();
        }
        if (PAYMENT_TYPE_ACH_DEBIT.equals(paymentType)) {
            return productSummaryPresentationRestClient.getAchDebitArrangements();
        }
        if (PAYMENT_TYPE_US_FOREIGN_WIRE.equals(paymentType)) {
            return productSummaryPresentationRestClient.getUSForeignWireArrangements();
        }
        if (PAYMENT_TYPE_US_DOMESTIC_WIRE.equals(paymentType)) {
            return productSummaryPresentationRestClient.getUsDomesticWireArrangements();
        }
        if (PAYMENT_TYPE_US_CROSS_BORDER_WIRE.equals(paymentType)) {
            return productSummaryPresentationRestClient.getUsCrossBorderWireArrangements();
        }
        if (PAYMENT_TYPE_US_FX_FOREIGN_WIRE.equals(paymentType)) {
            return productSummaryPresentationRestClient.getUsFxForeignWireArrangements();
        }
        if (PAYMENT_TYPE_FCY_WIRE.equals(paymentType)) {
            return productSummaryPresentationRestClient.getFcyWireArrangements();
        }
        throw new IllegalArgumentException("Unknown payment type " + paymentType);
    }
}
