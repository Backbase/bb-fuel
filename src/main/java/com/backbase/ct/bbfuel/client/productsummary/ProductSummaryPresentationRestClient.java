package com.backbase.ct.bbfuel.client.productsummary;

import static com.backbase.ct.bbfuel.data.CommonConstants.ACH_DEBIT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PAYMENTS_RESOURCE_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PRIVILEGE_CREATE;
import static com.backbase.ct.bbfuel.data.CommonConstants.PRIVILEGE_VIEW;
import static com.backbase.ct.bbfuel.data.CommonConstants.PRODUCT_SUMMARY_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PRODUCT_SUMMARY_RESOURCE_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.ct.bbfuel.dto.ProductSummaryQueryParameters;
import com.backbase.dbs.productsummary.presentation.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSummaryPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_PRODUCT_SUMMARY = "/productsummary";
    private static final String ENDPOINT_CONTEXT_ARRANGEMENTS = ENDPOINT_PRODUCT_SUMMARY + "/context/arrangements";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getProducts() + "/" + CLIENT_API);
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getProductSummaryArrangements() {
        return Arrays.asList(getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
            .withBusinessFunction(PRODUCT_SUMMARY_FUNCTION_NAME)
            .withResourceName(PRODUCT_SUMMARY_RESOURCE_NAME)
            .withPrivilege(PRIVILEGE_VIEW)
            .withSize(999)
            .withOrderBy("name"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ArrangementsByBusinessFunctionGetResponseBody[].class));
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getSepaCtArrangements() {
        ArrangementsByBusinessFunctionGetResponseBody[] arrangements = getProductSummaryContextArrangements(
            new ProductSummaryQueryParameters()
                .withBusinessFunction(SEPA_CT_FUNCTION_NAME)
                .withResourceName(PAYMENTS_RESOURCE_NAME)
                .withPrivilege(PRIVILEGE_CREATE)
                .withDebitAccount(true)
                .withCreditAccount(true)
                .withSize(999)
                .withOrderBy("name"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ArrangementsByBusinessFunctionGetResponseBody[].class);

        // Make sure the Regex is in sync with payment-order-presentation-service/src/main/resources/application.yml (property: sepacountries)
        return Arrays.stream(arrangements)
            .filter(arrangement -> arrangement.getIBAN() != null)
        // Extra filter, since we only generate IBAN for SEPA
/*            .filter(arrangement -> arrangement.getIBAN()
                .matches(
                    "^(AT|BE|BG|CH|CY|CZ|DE|DK|EE|ES|FI|FR|GB|GI|GR|HR|HU|IE|IS|IT|LI|LT|LU|LV|MC|MT|NL|NO|PL|PT|RO|SE|SI|SK|SM)[a-zA-Z0-9_.-]*"))*/
            .collect(Collectors.toList());
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getUsDomesticWireArrangements() {
        return Arrays.asList(getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
            .withBusinessFunction(US_DOMESTIC_WIRE_FUNCTION_NAME)
            .withResourceName(PAYMENTS_RESOURCE_NAME)
            .withPrivilege(PRIVILEGE_CREATE)
            .withDebitAccount(true)
            .withCreditAccount(true)
            .withSize(999)
            .withOrderBy("name"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ArrangementsByBusinessFunctionGetResponseBody[].class));
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getAchDebitArrangements() {
        return Arrays.stream(getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
            .withBusinessFunction(ACH_DEBIT_FUNCTION_NAME)
            .withResourceName(PAYMENTS_RESOURCE_NAME)
            .withPrivilege(PRIVILEGE_CREATE)
            .withDebitAccount(true)
            .withCreditAccount(true)
            .withSize(999)
            .withOrderBy("name"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ArrangementsByBusinessFunctionGetResponseBody[].class))
                .filter(arrangement -> isValidCurrencyForAchDebit(arrangement.getCurrency())).collect(Collectors.toList());
    }

    private static boolean isValidCurrencyForAchDebit(String currency) {
        return currency.equals("USD") || currency.equals("CAD");
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getUSForeignWireArrangements() {
        return Arrays.asList(getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
            .withBusinessFunction(US_FOREIGN_WIRE_FUNCTION_NAME)
            .withResourceName(PAYMENTS_RESOURCE_NAME)
            .withPrivilege(PRIVILEGE_CREATE)
            .withDebitAccount(true)
            .withCreditAccount(true)
            .withSize(999)
            .withOrderBy("name"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ArrangementsByBusinessFunctionGetResponseBody[].class));
    }

    private Response getProductSummaryContextArrangements(ProductSummaryQueryParameters queryParameters) {
        return requestSpec()
            .queryParam("businessFunction", queryParameters.getBusinessFunction())
            .queryParam("resourceName", queryParameters.getResourceName())
            .queryParam("privilege", queryParameters.getPrivilege())
            .queryParam("externalTransferAllowed", queryParameters.getExternalTransferAllowed())
            .queryParam("creditAccount", queryParameters.getCreditAccount())
            .queryParam("debitAccount", queryParameters.getDebitAccount())
            .queryParam("from", queryParameters.getFrom())
            .queryParam("size", queryParameters.getSize())
            .queryParam("orderBy", queryParameters.getOrderBy())
            .queryParam("direction", queryParameters.getDirection())
            .queryParam("searchTerm", queryParameters.getSearchTerm())
            .get(getPath(ENDPOINT_CONTEXT_ARRANGEMENTS));
    }

}
