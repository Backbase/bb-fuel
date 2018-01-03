package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PRODUCTSUMMARY_BASE_URI;

public class ProductIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String PRODUCT_INTEGRATION_SERVICE = "product-integration-service";
    private static final String ENDPOINT_PRODUCTS = "/products";

    public ProductIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_PRODUCTSUMMARY_BASE_URI), SERVICE_VERSION);
        setInitialPath(PRODUCT_INTEGRATION_SERVICE);
    }

    public Response ingestProduct(ProductsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_PRODUCTS));
    }
}
