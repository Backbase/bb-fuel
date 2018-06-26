package com.backbase.ct.dataloader.configurators;

import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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
            accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
                .withExternalLegalEntityId(null)
                .withExternalUserId(externalUserId)
                .withServiceAgreementId(internalServiceAgreementId)
                .withFunctionGroupId(functionGroupId)
                .withDataGroupIds(dataGroupIds))
                .then()
                .statusCode(SC_OK);

            LOGGER
                .info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
                    internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
        });
    }

    public void assignPermissions(String externalUserId, String internalServiceAgreementId, String functionGroupId,
        List<String> dataGroupIds) {
        accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
            .withExternalLegalEntityId(null)
            .withExternalUserId(externalUserId)
            .withServiceAgreementId(internalServiceAgreementId)
            .withFunctionGroupId(functionGroupId)
            .withDataGroupIds(dataGroupIds))
            .then()
            .statusCode(SC_OK);

        LOGGER.info("Permission assigned for service agreement [{}], user [{}], function group [{}], data groups {}",
            internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds);
    }
}
