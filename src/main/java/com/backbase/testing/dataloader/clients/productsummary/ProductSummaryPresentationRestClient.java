package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class ProductSummaryPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_PRODUCTSUMMARY_PRESENTATION_SERVICE = "/product-summary-presentation-service/" + SERVICE_VERSION + "/productsummary";
    private static final String ENDPOINT_ARRANGEMENTS = ENDPOINT_PRODUCTSUMMARY_PRESENTATION_SERVICE + "/arrangements";

    public ProductSummaryPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH));
    }

    public Response getProductSummaryArrangements() {
        return requestSpec()
                .get(ENDPOINT_ARRANGEMENTS);
    }
}
