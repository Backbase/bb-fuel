package com.backbase.ct.dataloader.clients.legalentity;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.exceptions.BadRequestException;
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

    public void ingestLegalEntityAndLogResponse(LegalEntitiesPostRequestBody legalEntity) {
        Response response = ingestLegalEntity(legalEntity);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrorCode()
                .equals("legalEntity.save.error.message.E_EX_ID_ALREADY_EXISTS")) {

            LOGGER.info(String.format("Legal entity [%s] already exists, skipped ingesting this legal entity",
                legalEntity.getExternalId()));
        } else if (response.statusCode() == SC_CREATED) {
            if (legalEntity.getParentExternalId() == null) {
                LOGGER.info(String.format("Root legal entity [%s] ingested", legalEntity.getExternalId()));
            } else {
                LOGGER
                    .info(String.format("Legal entity [%s] ingested under parent legal entity [%s]",
                        legalEntity.getExternalId(), legalEntity.getParentExternalId()));
            }
        } else {
            response.then().statusCode(SC_CREATED);
        }
    }

    @Override
    protected String composeInitialPath() {
        return LEGAL_ENTITY_INTEGRATION_SERVICE;
    }

    private Response ingestLegalEntity(LegalEntitiesPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_LEGAL_ENTITIES));
    }

}
