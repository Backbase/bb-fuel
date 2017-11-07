package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.data.DataAccessGroupsGetResponseBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACCESSGROUP_PRESENTATION_SERVICE = "/accessgroup-presentation-service/" + SERVICE_VERSION + "/accessgroups";
    private static final String ENDPOINT_FUNCTION_BY_LEGAL_ENTITY_ID = ENDPOINT_ACCESSGROUP_PRESENTATION_SERVICE + "/function?legalEntityId=";
    private static final String ENDPOINT_DATA_BY_LEGAL_ENTITY_ID_AND_TYPE = ENDPOINT_ACCESSGROUP_PRESENTATION_SERVICE + "/data?legalEntityId=%s&type=%s";

    private static String getEndpointDataGroupsByLegalEntityAndType(String internalLegalEntityId, String type) {
        return String.format(ENDPOINT_DATA_BY_LEGAL_ENTITY_ID_AND_TYPE, internalLegalEntityId, type);
    }

    public AccessGroupPresentationRestClient() {
        super(globalProperties.get(PROPERTY_INFRA_BASE_URI));
        setInitialPath(globalProperties.get(PROPERTY_GATEWAY_PATH));
    }

    public Response retrieveFunctionGroupsByLegalEntity(String internalLegalEntityId) {
        return requestSpec()
                .get(ENDPOINT_FUNCTION_BY_LEGAL_ENTITY_ID + internalLegalEntityId);
    }

    public Response retrieveDataGroupsByLegalEntityAndType(String internalLegalEntityId, String type) {
        return requestSpec()
                .get(getEndpointDataGroupsByLegalEntityAndType(internalLegalEntityId, type));
    }

    public List<String> retrieveAllDataGroupIdsByLegalEntity(String internalLegalEntityId) {
        DataAccessGroupsGetResponseBody[] dataGroups = retrieveDataGroupsByLegalEntityAndType(internalLegalEntityId, "arrangements")
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(DataAccessGroupsGetResponseBody[].class);

        List<String> dataGroupIds = new ArrayList<>();
        Arrays.stream(dataGroups)
                .forEach(dg -> dataGroupIds.add(dg.getDataAccessGroupId()));

        return dataGroupIds;
    }
}