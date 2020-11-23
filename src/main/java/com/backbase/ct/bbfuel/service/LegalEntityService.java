package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.legalentity.LegalEntityIntegrationRestClient;
import com.backbase.ct.bbfuel.util.ResponseUtils;
import com.backbase.dbs.accesscontrol.legalentity.client.v2.model.LegalEntityCreateItem;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalEntityService {

    @Setter
    @Getter
    private String rootAdmin;

    private final LegalEntityIntegrationRestClient legalEntityIntegrationRestClient;

    public String ingestLegalEntity(LegalEntityCreateItem legalEntity) {
        Response response = legalEntityIntegrationRestClient.ingestLegalEntity(legalEntity);

        if (ResponseUtils.isBadRequestExceptionMatching(response, "Legal Entity with given external Id already exists")) {

            log.info("Legal entity [{}] already exists, skipped ingesting this legal entity",
                legalEntity.getExternalId());

            if (legalEntity.getParentExternalId() == null) {
                return EXTERNAL_ROOT_LEGAL_ENTITY_ID;
            }

            return legalEntity.getExternalId();
        } else {
            response.then()
                .statusCode(SC_CREATED);

            log.info("Legal entity [{}] ingested", legalEntity.getExternalId());

            return legalEntity.getExternalId();
        }
    }

}
