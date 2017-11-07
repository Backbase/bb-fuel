package com.backbase.testing.dataloader.clients.legalentity;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class LegalEntityIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_LEGALENTITY_INTEGRATION_SERVICE = "/legalentity-integration-service/" + SERVICE_VERSION + "/legalentities";

    public LegalEntityIntegrationRestClient() {
        super(globalProperties.getString(CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI));
    }

    public Response ingestLegalEntity(LegalEntitiesPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(ENDPOINT_LEGALENTITY_INTEGRATION_SERVICE);
    }
}
