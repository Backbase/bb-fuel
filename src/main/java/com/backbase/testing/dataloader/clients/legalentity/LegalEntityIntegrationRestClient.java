package com.backbase.testing.dataloader.clients.legalentity;

import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;

public class LegalEntityIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String LEGALENTITY_INTEGRATION_SERVICE = "legalentity-integration-service";
    private static final String ENDPOINT_LEGALENTITIES = "/legalentities";

    public LegalEntityIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_ENTITLEMENTS_BASE_URI), SERVICE_VERSION);
        setInitialPath(LEGALENTITY_INTEGRATION_SERVICE);
    }

    public Response ingestLegalEntity(LegalEntitiesPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_LEGALENTITIES));
    }
}
