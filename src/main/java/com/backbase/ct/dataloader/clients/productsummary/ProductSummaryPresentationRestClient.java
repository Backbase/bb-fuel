package com.backbase.ct.dataloader.clients.productsummary;

import static com.backbase.ct.dataloader.data.CommonConstants.PAYMENTS_RESOURCE_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.PRIVILEGE_CREATE;
import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.ct.dataloader.dto.ProductSummaryQueryParameters;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProductSummaryPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String PRODUCT_SUMMARY_PRESENTATION_SERVICE = "product-summary-presentation-service";
    private static final String ENDPOINT_PRODUCT_SUMMARY = "/productsummary";
    private static final String ENDPOINT_ARRANGEMENTS = ENDPOINT_PRODUCT_SUMMARY + "/arrangements";
    private static final String ENDPOINT_CONTEXT_ARRANGEMENTS = ENDPOINT_PRODUCT_SUMMARY + "/context/arrangements";

    public ProductSummaryPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response getProductSummaryArrangements() {
        return requestSpec()
            .get(getPath(ENDPOINT_ARRANGEMENTS));
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
            .filter(arrangement -> arrangement.getIBAN()
                .matches(
                    "^(AT|BE|BG|CH|CY|CZ|DE|DK|EE|ES|FI|FR|GB|GI|GR|HR|HU|IE|IS|IT|LI|LT|LU|LV|MC|MT|NL|NO|PL|PT|RO|SE|SI|SK|SM)[a-zA-Z0-9_.-]*"))
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

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + PRODUCT_SUMMARY_PRESENTATION_SERVICE;
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