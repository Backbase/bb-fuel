package com.backbase.testing.dataloader.clients.legalentity;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.data.CommonConstants;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

public class LegalEntityPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_LEGALENTITY_PRESENTATION_SERVICE = "/legalentity-presentation-service/" + SERVICE_VERSION + "/legalentities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGALENTITY_PRESENTATION_SERVICE + "/external/%s";

    public static String getEndpointLegalEntityInternalByExternal(String externalId) {
        return String.format(ENDPOINT_EXTERNAL, externalId);
    }

    public LegalEntityPresentationRestClient() {
        super(globalProperties.getString(CommonConstants.PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.getString(CommonConstants.PROPERTY_GATEWAY_PATH));
    }

    public Response retrieveLegalEntityByExternalId(String externalLegalEntityId) {
        return requestSpec().get(getEndpointLegalEntityInternalByExternal(externalLegalEntityId));
    }
}
