package com.backbase.ct.bbfuel.client.pfm;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import com.backbase.dbs.arrangement.integration.rest.spec.v2.arrangements.ArrangementsPostResponseBody;
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

    public void createArrangedLegalEntity(ArrangementsPostResponseBody arrangement, LegalEntityBase legalEntity) {
        requestSpec()
            .contentType(ContentType.JSON)
            .body(new ArrangedLegalEntityRequest(arrangement.getId(), legalEntity.getId()))
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
