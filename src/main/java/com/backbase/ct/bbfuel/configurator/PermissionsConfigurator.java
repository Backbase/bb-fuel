package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MULTI_STATUS;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import io.restassured.response.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsConfigurator.class);

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;

    public void assignAllFunctionDataGroupsToUserAndServiceAgreement(String externalUserId,
        String internalServiceAgreementId) {
        List<String> functionGroupIds = accessGroupPresentationRestClient
            .retrieveFunctionGroupIdsByServiceAgreement(internalServiceAgreementId);
        List<String> dataGroupIds = accessGroupPresentationRestClient
            .retrieveDataGroupIdsByServiceAgreement(internalServiceAgreementId);

        functionGroupIds.forEach(functionGroupId -> {
            accessGroupIntegrationRestClient.assignPermissions(
                externalUserId,
                internalServiceAgreementId,
                functionGroupId,
                dataGroupIds)
                .then()
                .statusCode(SC_OK);

            LOGGER
                .info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                    internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        });
    }

    public void assignPermissions(String externalUserId, String externalServiceAgreementId, String functionGroupId,
        List<String> dataGroupIds) {
        Response response = accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            externalServiceAgreementId,
            functionGroupId,
            dataGroupIds);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrors()
                .get(0)
                .getMessage()
                .equals("dataAccessGroup.assign.error.message.E_ASSIGNED")) {

            LOGGER.info(
                "Data groups already assigned to service agreement [{}], user [{}], function group [{}], skipped assigning data group ids {}",
                externalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        } else if (response.statusCode() == SC_MULTI_STATUS && response.then().extract()
            .as(BatchResponseItem[].class)[0].getStatus().equals(BatchResponseStatusCode.HTTP_STATUS_OK)) {

            LOGGER
                .info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                    externalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        } else {

            LOGGER.info(
                "Failed assigning data groups to service agreement [{}], user [{}], function group [{}], with data group ids {}",
                externalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        }
    }
}
