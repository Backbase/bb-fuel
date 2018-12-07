package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MULTI_STATUS;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.IntegrationIdentifier;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.IntegrationFunctionGroupDataGroup;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsConfigurator.class);

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    //TODO Refactor logging
    public void assignPermissions(String externalUserId, String externalServiceAgreementId,
        List<IntegrationFunctionGroupDataGroup> functionGroupDataGroups) {
        Response response = accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            externalServiceAgreementId,
            functionGroupDataGroups);

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrors()
                .get(0)
                .getMessage()
                .equals("dataAccessGroup.assign.error.message.E_ASSIGNED")) {

            functionGroupDataGroups.forEach(functionGroupDataGroup -> {
                List<String> ids = functionGroupDataGroup.getDataGroupIdentifiers().stream()
                    .map(IntegrationIdentifier::getIdIdentifier).collect(
                        Collectors.toList());

                LOGGER.info(
                    "Data groups already assigned to service agreement [{}], user [{}], function group [{}], skipped assigning data group ids {}",
                    externalServiceAgreementId, externalUserId, functionGroupDataGroup.getFunctionGroupIdentifier(),
                    ids);
            });
        } else if (response.statusCode() == SC_MULTI_STATUS && response.then().extract()
            .as(BatchResponseItem[].class)[0].getStatus().equals(BatchResponseStatusCode.HTTP_STATUS_OK)) {

            functionGroupDataGroups.forEach(functionGroupDataGroup -> {
                List<String> ids = functionGroupDataGroup.getDataGroupIdentifiers().stream()
                    .map(IntegrationIdentifier::getIdIdentifier).collect(
                        Collectors.toList());
                LOGGER
                    .info(
                        "Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                        externalServiceAgreementId, externalUserId, functionGroupDataGroup.getFunctionGroupIdentifier(),
                        ids);
            });
        } else {

            functionGroupDataGroups.forEach(functionGroupDataGroup -> {
                List<String> ids = functionGroupDataGroup.getDataGroupIdentifiers().stream()
                    .map(IntegrationIdentifier::getIdIdentifier).collect(
                        Collectors.toList());
                LOGGER.info(
                    "Failed assigning data groups to service agreement [{}], user [{}], function group [{}], with data group ids {}",
                    externalServiceAgreementId, externalUserId, functionGroupDataGroup.getFunctionGroupIdentifier(),
                    ids);
            });
        }
    }
}
