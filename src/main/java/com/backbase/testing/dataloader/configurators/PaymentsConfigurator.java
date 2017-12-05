package com.backbase.testing.dataloader.configurators;

import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.InitiatePaymentOrder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ProductSummaryByLegalEntityIdGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.UserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.payment.PaymentOrderPresentationRestClient;
import com.backbase.testing.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.data.PaymentsDataGenerator;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PAYMENTS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PAYMENTS_MIN;
import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

public class PaymentsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private Random random = new Random();
    private PaymentOrderPresentationRestClient paymentOrderPresentationRestClient = new PaymentOrderPresentationRestClient();
    private PaymentsDataGenerator paymentsDataGenerator = new PaymentsDataGenerator();
    private LoginRestClient loginRestClient = new LoginRestClient();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private ProductSummaryPresentationRestClient productSummaryPresentationRestClient = new ProductSummaryPresentationRestClient();

    private static final String ENTITLEMENTS_PAYMENTS_FUNCTION_NAME = "SEPA CT";
    private static final String ENTITLEMENTS_PAYMENTS_RESOURCE_NAME = "Payments";
    private static final String PRIVILEGE_CREATE = "create";

    public void ingestPaymentOrders(String externalUserId) {
        List<ProductSummaryByLegalEntityIdGetResponseBody> sepaArrangements = getSepaArrangementsByExternalUserId(externalUserId);

        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_PAYMENTS_MIN), globalProperties.getInt(PROPERTY_PAYMENTS_MAX)); i++) {
            ProductSummaryByLegalEntityIdGetResponseBody randomArrangement = sepaArrangements.get(random.nextInt(sepaArrangements.size()));
            InitiatePaymentOrder initiatePaymentOrder = paymentsDataGenerator.generateInitiatePaymentOrder(randomArrangement.getId());
            paymentOrderPresentationRestClient.initiatePaymentOrder(initiatePaymentOrder)
                    .then()
                    .statusCode(SC_ACCEPTED);

            LOGGER.info(String.format("Payment order ingested for debtor account [%s]", initiatePaymentOrder.getDebtorAccount().getIdentification().getIdentification()));
        }
    }

    private List<ProductSummaryByLegalEntityIdGetResponseBody> getSepaArrangementsByExternalUserId(String externalUserId) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        String internalUserId = userPresentationRestClient.getUserByExternalId(externalUserId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(UserGetResponseBody.class)
                .getId();

        ArrangementPrivilegesGetResponseBody[] arrangementPrivilegesGetResponseBodies = accessGroupPresentationRestClient.getListOfArrangementsWithPrivilegesForUser(internalUserId, ENTITLEMENTS_PAYMENTS_FUNCTION_NAME, ENTITLEMENTS_PAYMENTS_RESOURCE_NAME, PRIVILEGE_CREATE)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ArrangementPrivilegesGetResponseBody[].class);

        loginRestClient.login(externalUserId, externalUserId);
        ProductSummaryByLegalEntityIdGetResponseBody[] ProductSummaryByLegalEntityIdGetResponseBodies = productSummaryPresentationRestClient.getProductSummaryArrangements()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ProductSummaryByLegalEntityIdGetResponseBody[].class);

        List<ProductSummaryByLegalEntityIdGetResponseBody> paymentsArrangements = new ArrayList<>();

        for (ArrangementPrivilegesGetResponseBody arrangementPrivilegesGetResponseBody : arrangementPrivilegesGetResponseBodies) {
            ProductSummaryByLegalEntityIdGetResponseBody productSummaryByLegalEntityIdGetResponseBody = Arrays.stream(ProductSummaryByLegalEntityIdGetResponseBodies)
                    .filter(ps -> ps.getId()
                            .equals(arrangementPrivilegesGetResponseBody.getArrangementId()))
                    .findFirst()
                    .orElse(null);

            if (productSummaryByLegalEntityIdGetResponseBody != null) {
                paymentsArrangements.add(productSummaryByLegalEntityIdGetResponseBody);
            }
        }
        // Make sure the Regex is in sync with payment-order-presentation-service/src/main/resources/application.yml (property: sepacountries)
        return paymentsArrangements.stream()
                .filter(arrangement -> arrangement.getIBAN()
                        .matches("^(AT|BE|BG|CH|CY|CZ|DE|DK|EE|ES|FI|FR|GB|GI|GR|HR|HU|IE|IS|IT|LI|LT|LU|LV|MC|MT|NL|NO|PL|PT|RO|SE|SI|SK|SM)[a-zA-Z0-9_.-]*"))
                .collect(Collectors.toList());
    }
}