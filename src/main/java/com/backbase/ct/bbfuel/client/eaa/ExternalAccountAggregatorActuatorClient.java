package com.backbase.ct.bbfuel.client.eaa;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import io.restassured.http.ContentType;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalAccountAggregatorActuatorClient extends RestClient {

    private final BbFuelConfiguration config;

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getExternalAccountAggregator().replace("/client-api", "/actuator"));
    }

    public void makeAggregationDataAvailable(LegalEntityBase legalEntity) {
        requestSpec()
            .contentType(ContentType.JSON)
            .post("/aggregation/" + legalEntity.getId())
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
