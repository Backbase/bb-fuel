package com.backbase.ct.dataloader.client.legalentity;

import com.backbase.ct.dataloader.client.common.AbstractRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LegalEntityIntegrationRestClient extends AbstractRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityIntegrationRestClient.class);

    private static final String ENTITLEMENTS = globalProperties
        .getString(CommonConstants.PROPERTY_ACCESS_CONTROL_BASE_URI);
    private static final String SERVICE_VERSION = "v2";
    private static final String LEGAL_ENTITY_INTEGRATION_SERVICE = "legalentity-integration-service";
    private static final String ENDPOINT_LEGAL_ENTITIES = "/legalentities";

    public LegalEntityIntegrationRestClient() {
        super(ENTITLEMENTS, SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response ingestLegalEntity(LegalEntitiesPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_LEGAL_ENTITIES));
    }

    @Override
    protected String composeInitialPath() {
        return LEGAL_ENTITY_INTEGRATION_SERVICE;
    }

}
