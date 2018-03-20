package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.payment.PaymentOrderPresentationRestClient;
import com.backbase.ct.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.PaymentsDataGenerator;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.IdentifiedPaymentOrder;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.http.HttpStatus.SC_ACCEPTED;

public class PaymentsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private Random random = new Random();
    private PaymentOrderPresentationRestClient paymentOrderPresentationRestClient = new PaymentOrderPresentationRestClient();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private ProductSummaryPresentationRestClient productSummaryPresentationRestClient = new ProductSummaryPresentationRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();

    public void ingestPaymentOrders(String externalUserId) {
        IdentifiedPaymentOrder.PaymentType paymentType = IdentifiedPaymentOrder.PaymentType.values()[random.nextInt(IdentifiedPaymentOrder.PaymentType.values().length)];

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        List<ArrangementsByBusinessFunctionGetResponseBody> sepaCtArrangements = productSummaryPresentationRestClient.getSepaCtArrangements();
        List<ArrangementsByBusinessFunctionGetResponseBody> usDomesticWireArrangements = productSummaryPresentationRestClient.getUsDomesticWireArrangements();

        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MIN), globalProperties.getInt(CommonConstants.PROPERTY_PAYMENTS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            ArrangementsByBusinessFunctionGetResponseBody randomArrangement;

            if (paymentType.equals(IdentifiedPaymentOrder.PaymentType.SEPA_CREDIT_TRANSFER)) {
                randomArrangement = sepaCtArrangements.get(random.nextInt(sepaCtArrangements.size()));
            } else {
                randomArrangement = usDomesticWireArrangements.get(random.nextInt(usDomesticWireArrangements.size()));
            }

            InitiatePaymentOrder initiatePaymentOrder = PaymentsDataGenerator.generateInitiatePaymentOrder(randomArrangement.getId(), paymentType);
            paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
                    .then()
                    .statusCode(SC_ACCEPTED);

            LOGGER.info(String.format("Payment order ingested for debtor account [%s] for user [%s]", initiatePaymentOrder.getDebtorAccount().getIdentification().getIdentification(), externalUserId));
        });
    }
}