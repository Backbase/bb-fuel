package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalEntityService {

    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;

    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;

    public String ingestLegalEntity(LegalEntitiesPostRequestBody legalEntity) {
        Response response = legalEntityIntegrationRestClient.ingestLegalEntity(legalEntity);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(com.backbase.presentation.legalentity.rest.spec.v2.legalentities.exceptions.BadRequestException.class)
                .getErrorCode()
                .matches("legalEntity.save.error.message.(.*)_ALREADY_EXISTS")) {

            log.info("Legal entity [{}] already exists, skipped ingesting this legal entity",
                legalEntity.getExternalId());

            if (legalEntity.getParentExternalId() == null) {
                return EXTERNAL_ROOT_LEGAL_ENTITY_ID;
            }

            // Legal entity name is unique in the system
            LegalEntitiesGetResponseBody existingLegalEntity = legalEntityPresentationRestClient
                .retrieveLegalEntities()
                .stream()
                .filter(legalEntitiesGetResponseBody ->
                    legalEntity.getExternalId().equals(legalEntitiesGetResponseBody.getExternalId()) ||
                    legalEntity.getName().equals(legalEntitiesGetResponseBody.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                    String.format("No existing legal entity found by name [%s]", legalEntity.getName())));

            return existingLegalEntity.getExternalId();
        } else {
            response.then()
                .statusCode(SC_CREATED);

            log.info("Legal entity [{}] ingested", legalEntity.getExternalId());

            return legalEntity.getExternalId();
        }
    }

}
