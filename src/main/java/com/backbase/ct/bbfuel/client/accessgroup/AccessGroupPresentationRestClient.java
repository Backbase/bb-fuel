package com.backbase.ct.bbfuel.client.accessgroup;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessGroupPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CLIENT_API = "client-api";
    private static final String ENDPOINT_ACCESS_GROUPS = "/accessgroups";
    private static final String ENDPOINT_USER_ACCESS = ENDPOINT_ACCESS_GROUPS + "/users";
    private static final String ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID =
        ENDPOINT_ACCESS_GROUPS + "/function-groups?serviceAgreementId=%s";
    private static final String ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE =
        ENDPOINT_ACCESS_GROUPS + "/data-groups?serviceAgreementId=%s&type=%s";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getAccessgroup() + "/" + CLIENT_API);
    }

    public List<FunctionGroupsGetResponseBody> retrieveFunctionGroupsByServiceAgreement(String internalServiceAgreementId) {
        return asList(requestSpec()
            .get(String.format(getPath(ENDPOINT_FUNCTION_BY_SERVICE_AGREEMENT_ID), internalServiceAgreementId))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(FunctionGroupsGetResponseBody[].class));
    }

    public List<String> retrieveFunctionGroupIdsByServiceAgreement(String internalServiceAgreementId) {
        return retrieveFunctionGroupsByServiceAgreement(internalServiceAgreementId).stream()
            .map(FunctionGroupsGetResponseBody::getId)
            .collect(Collectors.toList());
    }

    public List<DataGroupsGetResponseBody> retrieveDataGroupsByServiceAgreement(String internalServiceAgreement) {
        return asList(requestSpec()
            .get(String.format(getPath(ENDPOINT_DATA_BY_SERVICE_AGREEMENT_ID_AND_TYPE), internalServiceAgreement,
                    "ARRANGEMENTS"))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(DataGroupsGetResponseBody[].class));
    }

    public List<String> retrieveDataGroupIdsByServiceAgreement(String internalServiceAgreement) {
        return retrieveDataGroupsByServiceAgreement(internalServiceAgreement).stream()
            .map(DataGroupsGetResponseBody::getId)
            .collect(Collectors.toList());
    }

    public List<String> getDataGroupIdsByFunctionId(String internalServiceAgreementId, String internalUserId,
        String functionId) {
        List<String> functionGroupIds = getFunctionGroupsByFunctionId(internalServiceAgreementId, functionId)
            .stream()
            .map(FunctionGroupsGetResponseBody::getId)
            .collect(Collectors.toList());

        List<UsersGetResponseBody> userAccesses = getUserAccessByUserId(internalUserId);

        return userAccesses.stream()
            .filter(userAccessGetResponseBody -> internalServiceAgreementId
                .equals(userAccessGetResponseBody.getServiceAgreementId()))
            .flatMap(userAccessGetResponseBody -> userAccessGetResponseBody.getDataAccessGroupsByFunctionAccessGroup()
                .stream())
            .filter(dataAccessGroupsByFunctionAccessGroup -> functionGroupIds
                .contains(dataAccessGroupsByFunctionAccessGroup.getFunctionAccessGroupId()))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No function group found"))
            .getDataAccessGroupIds();
    }

    public List<FunctionGroupsGetResponseBody> getFunctionGroupsByFunctionId(String internalServiceAgreementId,
        String functionId) {
        return retrieveFunctionGroupsByServiceAgreement(
            internalServiceAgreementId).stream()
            .filter(fg -> fg.getPermissions()
                .stream()
                .anyMatch(permission -> functionId.equals(permission.getFunctionId())))
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

}
