package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.exceptions.BadRequestException;
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

    public void assignPermissions(String externalUserId, String internalServiceAgreementId, String functionGroupId,
        List<String> dataGroupIds) {
        Response response = accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            internalServiceAgreementId,
            functionGroupId,
            dataGroupIds);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrorCode()
                .equals("dataAccessGroup.assign.error.message.E_ASSIGNED")) {

            LOGGER.info("Data groups already assigned to service agreement [{}], user [{}], function group [{}], skipped assigning data group ids {}",
                internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        } else {
            response.then()
                .statusCode(SC_OK);

            LOGGER
                .info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                    internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        }


    }
}
