package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static org.apache.http.HttpStatus.SC_MULTI_STATUS;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.BatchResponseItem;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationDataGroupIdentifier;
import com.backbase.dbs.accesscontrol.accessgroup.integration.v3.model.IntegrationFunctionGroupDataGroup;
import com.backbase.dbs.arrangement.integration.inbound.api.v3.model.BatchResponseStatusCode;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionsConfigurator {

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    public void assignPermissions(String externalUserId, String externalServiceAgreementId,
        List<IntegrationFunctionGroupDataGroup> functionGroupDataGroups) {
        Response response = accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            externalServiceAgreementId,
            functionGroupDataGroups);

        if (isBadRequestException(response, "dataAccessGroup.assign.error.message.E_ASSIGNED")) {

            functionGroupDataGroups.forEach(group -> {
                List<String> ids = group.getDataGroupIdentifiers().stream()
                    .map(IntegrationDataGroupIdentifier::getIdIdentifier).collect(
                        Collectors.toList());
                log.info(
                    "Data groups already assigned to service agreement [{}], user [{}], function group [{}], skipped assigning data group ids {}",
                    externalServiceAgreementId, externalUserId, group.getFunctionGroupIdentifier().getIdIdentifier(), ids);
            });
        } else if (response.statusCode() == SC_MULTI_STATUS && response.then().extract()
            .as(BatchResponseItem[].class)[0].getStatus().equals(BatchResponseStatusCode.HTTP_STATUS_OK)) {

            functionGroupDataGroups.forEach(group -> {
                List<String> ids = group.getDataGroupIdentifiers().stream()
                    .map(IntegrationDataGroupIdentifier::getIdIdentifier)
                    .collect(Collectors.toList());
                log.info(
                    "Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                        externalServiceAgreementId, externalUserId, group.getFunctionGroupIdentifier().getIdIdentifier(), ids);
            });
        } else {
            functionGroupDataGroups.forEach(group -> {
                List<String> ids = group.getDataGroupIdentifiers().stream()
                    .map(IntegrationDataGroupIdentifier::getIdIdentifier)
                    .collect(Collectors.toList());
                log.error(
                    "Failed assigning data groups to service agreement [{}], user [{}], function group [{}], with data group ids {}",
                    externalServiceAgreementId, externalUserId, group.getFunctionGroupIdentifier().getIdIdentifier(), ids);
                throw new RuntimeException("Failed assigning data groups to service agreement");
            });
        }
    }
}
