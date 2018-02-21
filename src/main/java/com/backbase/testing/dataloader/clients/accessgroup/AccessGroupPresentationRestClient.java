package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESSGROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESSGROUPS = "/accessgroups";
    private static final String ENDPOINT_CONFIG_FUNCTIONS = ENDPOINT_ACCESSGROUPS + "/config/functions";
    private static final String ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID = ENDPOINT_ACCESSGROUPS + "/function-groups?serviceAgreementId=%s";
    private static final String ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE = ENDPOINT_ACCESSGROUPS + "/data-groups?serviceAgreementId=%s&type=%s";

    public AccessGroupPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + ACCESSGROUP_PRESENTATION_SERVICE);
    }

    public Response retrieveFunctionGroupsByServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID), internalServiceAgreementId));
    }

    public List<String> retrieveFunctionGroupIdsByServiceAgreement(String internalServiceAgreement) {
        FunctionGroupsGetResponseBody[] functionGroups = retrieveFunctionGroupsByServiceAgreement(internalServiceAgreement)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionGroupsGetResponseBody[].class);

        return Arrays.stream(functionGroups)
            .map(FunctionGroupsGetResponseBody::getId)
            .collect(Collectors.toList());
    }

    public String getFunctionGroupIdByServiceAgreementIdAndFunctionName(String internalServiceAgreementId, String functionName) {
        String functionId = getFunctionIdForFunctionName(retrieveFunctions()
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionsGetResponseBody[].class), functionName);

        FunctionGroupsGetResponseBody[] functionGroups = retrieveFunctionGroupsByServiceAgreement(internalServiceAgreementId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionGroupsGetResponseBody[].class);

        FunctionGroupsGetResponseBody functionGroup = Arrays.stream(functionGroups)
            .filter(fg -> fg.getPermissions()
                .get(0)
                .getFunctionId()
                .equals(functionId))
            .findFirst()
            .orElse(null);

        if (functionGroup == null) {
            return null;
        } else {
            return functionGroup.getId();
        }
    }

    public Response retrieveDataGroupsByServiceAgreementAndType(String internalServiceAgreement, String type) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE), internalServiceAgreement, type));
    }

    public List<String> retrieveDataGroupIdsByServiceAgreement(String internalServiceAgreement) {
        DataGroupsGetResponseBody[] dataGroups = retrieveDataGroupsByServiceAgreementAndType(internalServiceAgreement, "ARRANGEMENTS")
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(DataGroupsGetResponseBody[].class);

        return Arrays.stream(dataGroups)
            .map(DataGroupsGetResponseBody::getId)
            .collect(Collectors.toList());
    }

    private Response retrieveFunctions() {
        return requestSpec()
                .get(getPath(ENDPOINT_CONFIG_FUNCTIONS));
    }

    private String getFunctionIdForFunctionName(FunctionsGetResponseBody[] functions, String functionName) {
        Optional<String> functionId = Arrays.stream(functions)
                .filter(e -> e.getName()
                        .equals(functionName))
                .map(FunctionsGetResponseBody::getFunctionId)
                .findFirst();
        return functionId.orElse(null);
    }
}