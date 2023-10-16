package com.backbase.ct.bbfuel.client.pfm;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.user.manager.models.v2.LegalEntity;
import io.restassured.http.ContentType;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PocketTailorActuatorClient extends RestClient {

    private final BbFuelConfiguration config;

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getPockets().replace("/client-api", "/actuator"));
    }

    /**
     * Create entry in table pocket_tailor.arranged_legal_entity
     * @param arrangementId arrangement id
     * @param legalEntity legal entity
     */
    public void createArrangedLegalEntity(String arrangementId, LegalEntity legalEntity) {
        requestSpec()
            .contentType(ContentType.JSON)
            .body(new ArrangedLegalEntityRequest(arrangementId, legalEntity.getId()))
            .post("/arranged-legal-entities")
            .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Getter
    @AllArgsConstructor
    private static class ArrangedLegalEntityRequest {
        private final String arrangementId;
        private final String legalEntityId;
    }
}
