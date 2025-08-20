package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.util.ResponseUtils.isBadRequestException;
import static org.apache.http.HttpStatus.SC_MULTI_STATUS;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.BatchResponseItemExtended;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.DataGroupNameIdentifier;
import com.backbase.dbs.accesscontrol.ac_assign_permissions.integration.v1.model.UserPermissionItem;
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
        List<UserPermissionItem> functionGroupDataGroups) {
        Response response = accessGroupIntegrationRestClient.assignPermissions(
            externalUserId,
            externalServiceAgreementId,
            functionGroupDataGroups);

        if (isBadRequestException(response, "dataAccessGroup.assign.error.message.E_ASSIGNED")) {

            functionGroupDataGroups.forEach(group -> {
                List<String> ids = group.getDataGroups().stream()
                    .map(DataGroupNameIdentifier::getName).collect(
                        Collectors.toList());
                log.info(
                    "Data groups already assigned to service agreement [{}], user [{}], function group {}, skipped assigning data group names {}",
                    externalServiceAgreementId, externalUserId, group.getFunctionGroup().getName(), ids);
            });
        } else if (response.statusCode() == SC_MULTI_STATUS && response.then().extract()
            .as(BatchResponseItemExtended[].class)[0].getStatus()
            .equals(BatchResponseItemExtended.StatusEnum.HTTP_STATUS_OK)) {

            functionGroupDataGroups.forEach(group -> {
                List<String> ids = group.getDataGroups().stream()
                    .map(DataGroupNameIdentifier::getName)
                    .collect(Collectors.toList());
                log.info(
                    "Permission assigned for service agreement [{}], user [{}], function group {}, data group names {}",
                    externalServiceAgreementId, externalUserId, group.getFunctionGroup().getName(), ids);
            });
        } else {
            functionGroupDataGroups.forEach(group -> {
                List<String> ids = group.getDataGroups().stream()
                    .map(DataGroupNameIdentifier::getName)
                    .collect(Collectors.toList());
                log.error(
                    "Failed assigning data groups to service agreement [{}], user [{}], function group {}, with data group names {}",
                    externalServiceAgreementId, externalUserId, group.getFunctionGroup().getName(), ids);
                throw new RuntimeException("Failed assigning data groups to service agreement");
            });
        }
    }
}
