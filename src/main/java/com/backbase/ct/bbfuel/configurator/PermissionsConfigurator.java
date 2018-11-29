package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.exceptions.BadRequestException;
import io.restassured.response.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionsConfigurator {

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

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

            log.info("Data groups already assigned to service agreement [{}], user [{}], function group [{}], skipped assigning data group ids {}",
                internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        } else {
            response.then()
                .statusCode(SC_OK);

            log.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        }
    }
}
