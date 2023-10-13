package com.backbase.ct.bbfuel.client.accessgroup;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.accesscontrol.client.v3.model.DataGroupItem;
import com.backbase.dbs.accesscontrol.client.v3.model.FunctionGroupItem;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessGroupPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v3";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_ACCESS = ENDPOINT_ACCESS_GROUPS + "/users";
    private static final String ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID =
        ENDPOINT_ACCESS_GROUPS + "/function-groups?serviceAgreementId=%s";
    private static final String ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE =
        ENDPOINT_ACCESS_GROUPS + "/data-groups?serviceAgreementId=%s&type=%s";
    private static final String ENDPOINT_UPDATE_DATAGROUP = ENDPOINT_ACCESS_GROUPS + "/data-groups/%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getAccessgroup() + "/" + CLIENT_API);
    }

    public List<FunctionGroupItem> retrieveFunctionGroupsByServiceAgreement(String internalServiceAgreementId) {
        return asList(requestSpec()
            .get(String.format(getPath(ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID), internalServiceAgreementId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionGroupItem[].class));
    }

    public List<DataGroupItem> retrieveDataGroupsByServiceAgreement(String internalServiceAgreement) {
        return asList(requestSpec()
            .get(String.format(getPath(ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE), internalServiceAgreement,
                    "ARRANGEMENTS"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(DataGroupItem[].class));
    }

    /**
     * Update data group.
     * @param dataGroupId data group id
     * @param body as DataGroupItem
     * @return Response
     */
    public Response updateDataGroup(String dataGroupId,
        DataGroupItem body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .put(getPath(String.format(ENDPOINT_UPDATE_DATAGROUP, dataGroupId)));
    }
}
