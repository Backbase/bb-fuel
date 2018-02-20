package com.backbase.testing.dataloader.clients.legalentity;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class LegalEntityPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String LEGALENTITY_PRESENTATION_SERVICE = "legalentity-presentation-service";
    private static final String ENDPOINT_LEGALENTITIES = "/legalentities";
    private static final String ENDPOINT_EXTERNAL = ENDPOINT_LEGALENTITIES + "/external/%s";
    private static final String ENDPOINT_SERVICEAGREEMENTS_MASTER = ENDPOINT_LEGALENTITIES + "/%s/serviceagreements/master";


    public LegalEntityPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + LEGALENTITY_PRESENTATION_SERVICE);
    }

    public Response retrieveLegalEntityByExternalId(String externalLegalEntityId) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_EXTERNAL), externalLegalEntityId));
    }

    public Response getMasterServiceAgreementOfLegalEntity(String internalLegalEntityId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_SERVICEAGREEMENTS_MASTER), internalLegalEntityId));
    }
}
