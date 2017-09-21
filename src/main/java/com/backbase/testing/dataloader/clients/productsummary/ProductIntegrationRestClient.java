package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ProductIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_PRODUCT_INTEGRATION_SERVICE = "/product-integration-service/" + SERVICE_VERSION + "/products";

    public ProductIntegrationRestClient() {
        super(globalProperties.get(CommonConstants.PROPERTY_PRODUCTSUMMARY_BASE_URI));
    }

    public Response ingestProduct(ProductsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_PRODUCT_INTEGRATION_SERVICE);
    }
}
