package com.backbase.ct.dataloader.service;

import static com.backbase.ct.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.dataloader.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalEntityService {

    private final static Logger LOGGER = LoggerFactory.getLogger(LegalEntityService.class);

    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;

    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;

    public String ingestLegalEntity(LegalEntitiesPostRequestBody legalEntity) {
        Response response = legalEntityIntegrationRestClient.ingestLegalEntity(legalEntity);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(com.backbase.presentation.legalentity.rest.spec.v2.legalentities.exceptions.BadRequestException.class)
                .getErrorCode()
                .equals("legalEntity.save.error.message.E_EX_ID_ALREADY_EXISTS")) {

            if (legalEntity.getParentExternalId() == null) {
                return EXTERNAL_ROOT_LEGAL_ENTITY_ID;
            }

            LegalEntitiesGetResponseBody existingLegalEntity = legalEntityPresentationRestClient
                .retrieveLegalEntities()
                .stream()
                .filter(legalEntitiesGetResponseBody -> legalEntity.getName().equals(legalEntitiesGetResponseBody.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No existing legal entity found by name [%s]", legalEntity.getName())));

            LOGGER.info("Legal entity [{}] already exists, skipped ingesting this legal entity", legalEntity.getExternalId());

            return existingLegalEntity.getExternalId();
        } else {
            response.then()
                .statusCode(SC_CREATED);

            LOGGER.info("Legal entity [{}] ingested", legalEntity.getExternalId());

            return legalEntity.getExternalId();
        }
    }

}
