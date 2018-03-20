package com.backbase.testing.dataloader.clients.accessgroup;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.testing.dataloader.clients.common.AbstractRestClient;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccessGroupPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_CONFIG_FUNCTIONS = ENDPOINT_ACCESS_GROUPS + "/config/functions";
    private static final String ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID = ENDPOINT_ACCESS_GROUPS + "/function-groups?serviceAgreementId=%s";
    private static final String ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE = ENDPOINT_ACCESS_GROUPS + "/data-groups?serviceAgreementId=%s&type=%s";

    public AccessGroupPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
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

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + ACCESS_GROUP_PRESENTATION_SERVICE;
    }

    private Response retrieveFunctionGroupsByServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID), internalServiceAgreementId));
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

    private Response retrieveDataGroupsByServiceAgreementAndType(String internalServiceAgreement, String type) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE), internalServiceAgreement, type));
    }

}