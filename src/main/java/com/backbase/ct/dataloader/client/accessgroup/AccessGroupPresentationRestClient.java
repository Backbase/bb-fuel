package com.backbase.ct.dataloader.client.accessgroup;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.client.common.AbstractRestClient;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersGetResponseBody;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AccessGroupPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESS_GROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_ACCESS = ENDPOINT_ACCESS_GROUPS + "/users";
    private static final String ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID =
        ENDPOINT_ACCESS_GROUPS + "/function-groups?serviceAgreementId=%s";
    private static final String ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE =
        ENDPOINT_ACCESS_GROUPS + "/data-groups?serviceAgreementId=%s&type=%s";

    public AccessGroupPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public List<String> retrieveFunctionGroupIdsByServiceAgreement(String internalServiceAgreement) {
        FunctionGroupsGetResponseBody[] functionGroups = retrieveFunctionGroupsByServiceAgreement(
            internalServiceAgreement)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionGroupsGetResponseBody[].class);

        return Arrays.stream(functionGroups)
            .map(FunctionGroupsGetResponseBody::getId)
            .collect(Collectors.toList());
    }

    public List<String> retrieveDataGroupIdsByServiceAgreement(String internalServiceAgreement) {
        DataGroupsGetResponseBody[] dataGroups = retrieveDataGroupsByServiceAgreementAndType(internalServiceAgreement,
            "ARRANGEMENTS")
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(DataGroupsGetResponseBody[].class);

        return Arrays.stream(dataGroups)
            .map(DataGroupsGetResponseBody::getId)
            .collect(Collectors.toList());
    }

    public List<String> getDataGroupIdsByFunctionId(String internalServiceAgreementId, String internalUserId, String functionId) {
        List<String> functionGroupIds = getFunctionGroupsByFunctionId(internalServiceAgreementId, functionId)
            .stream()
            .map(FunctionGroupsGetResponseBody::getId)
            .collect(Collectors.toList());

        List<UsersGetResponseBody> userAccesses = getUserAccessByUserId(internalUserId);

        return userAccesses.stream()
            .filter(userAccessGetResponseBody -> internalServiceAgreementId.equals(userAccessGetResponseBody.getServiceAgreementId()))
            .flatMap(userAccessGetResponseBody -> userAccessGetResponseBody.getDataAccessGroupsByFunctionAccessGroup()
                .stream())
            .filter(dataAccessGroupsByFunctionAccessGroup -> functionGroupIds
                .contains(dataAccessGroupsByFunctionAccessGroup.getFunctionAccessGroupId()))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No function group found"))
            .getDataAccessGroupIds();
    }


    public List<FunctionGroupsGetResponseBody> getFunctionGroupsByFunctionId(String internalServiceAgreementId, String functionId) {
        FunctionGroupsGetResponseBody[] functionGroups = retrieveFunctionGroupsByServiceAgreement(
            internalServiceAgreementId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionGroupsGetResponseBody[].class);

        return Arrays.stream(functionGroups)
            .filter(fg -> fg.getPermissions()
                .stream()
                .allMatch(permission -> functionId.equals(permission.getFunctionId())))
            .collect(Collectors.toList());
    }

    public List<UsersGetResponseBody> getUserAccessByUserId(String userId) {
        return asList(requestSpec()
            .queryParam("userId", userId)
            .get(getPath(ENDPOINT_USER_ACCESS))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(UsersGetResponseBody[].class));
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + ACCESS_GROUP_PRESENTATION_SERVICE;
    }

    private Response retrieveFunctionGroupsByServiceAgreement(String internalServiceAgreementId) {
        return requestSpec()
            .get(String.format(getPath(ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID), internalServiceAgreementId));
    }

    private Response retrieveDataGroupsByServiceAgreementAndType(String internalServiceAgreement, String type) {
        return requestSpec()
            .get(
                String.format(getPath(ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE), internalServiceAgreement, type));
    }

}
