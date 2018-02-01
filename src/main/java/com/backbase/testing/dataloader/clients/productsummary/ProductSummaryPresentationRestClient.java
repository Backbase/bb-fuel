package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.dto.ProductSummaryQueryParameters;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.backbase.testing.dataloader.data.CommonConstants.PAYMENTS_RESOURCE_NAME;
import static com.backbase.testing.dataloader.data.CommonConstants.PRIVILEGE_CREATE;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.testing.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static org.apache.http.HttpStatus.SC_OK;

public class ProductSummaryPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String PRODUCTSUMMARY_PRESENTATION_SERVICE = "product-summary-presentation-service";
    private static final String ENDPOINT_PRODUCTSUMMARY = "/productsummary";
    private static final String ENDPOINT_ARRANGEMENTS = ENDPOINT_PRODUCTSUMMARY + "/arrangements";
    private static final String ENDPOINT_CONTEXT_ARRANGEMENTS = ENDPOINT_PRODUCTSUMMARY + "/context/arrangements";

    public ProductSummaryPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + PRODUCTSUMMARY_PRESENTATION_SERVICE);
    }

    public Response getProductSummaryArrangements() {
        return requestSpec()
                .get(getPath(ENDPOINT_ARRANGEMENTS));
    }

    public Response getProductSummaryContextArrangements(ProductSummaryQueryParameters queryParameters) {
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

    public List<ArrangementsByBusinessFunctionGetResponseBody> getSepaCtArrangements() {
        ArrangementsByBusinessFunctionGetResponseBody[] arrangements = getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
                .withBusinessFunction(SEPA_CT_FUNCTION_NAME)
                .withResourceName(PAYMENTS_RESOURCE_NAME)
                .withPrivilege(PRIVILEGE_CREATE)
                .withSize(999)
                .withOrderBy("name"))
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ArrangementsByBusinessFunctionGetResponseBody[].class);

        // Make sure the Regex is in sync with payment-order-presentation-service/src/main/resources/application.yml (property: sepacountries)
        return Arrays.stream(arrangements)
                .filter(arrangement -> arrangement.getIBAN()
                        .matches("^(AT|BE|BG|CH|CY|CZ|DE|DK|EE|ES|FI|FR|GB|GI|GR|HR|HU|IE|IS|IT|LI|LT|LU|LV|MC|MT|NL|NO|PL|PT|RO|SE|SI|SK|SM)[a-zA-Z0-9_.-]*"))
                .collect(Collectors.toList());
    }

    public List<ArrangementsByBusinessFunctionGetResponseBody> getUsDomesticWireArrangements() {
        return Arrays.asList(getProductSummaryContextArrangements(new ProductSummaryQueryParameters()
                .withBusinessFunction(US_DOMESTIC_WIRE_FUNCTION_NAME)
                .withResourceName(PAYMENTS_RESOURCE_NAME)
                .withPrivilege(PRIVILEGE_CREATE)
                .withSize(999)
                .withOrderBy("name"))
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ArrangementsByBusinessFunctionGetResponseBody[].class));
    }
}
