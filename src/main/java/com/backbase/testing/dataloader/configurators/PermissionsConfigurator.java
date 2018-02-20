package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class PermissionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsConfigurator.class);

    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();

    public void assignAllFunctionDataGroupsToUserAndServiceAgreement(String externalUserId, String internalServiceAgreementId, String internalLegalEntityId) {
        FunctionAccessGroupsGetResponseBody[] functionGroups = accessGroupPresentationRestClient.retrieveFunctionGroupsByLegalEntity(internalLegalEntityId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionAccessGroupsGetResponseBody[].class);

        List<String> dataGroupIds = accessGroupPresentationRestClient.retrieveAllDataGroupIdsByLegalEntity(internalLegalEntityId);

        Arrays.stream(functionGroups).forEach(functionGroup -> {
                    accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
                            .withExternalLegalEntityId(null)
                            .withExternalUserId(externalUserId)
                            .withServiceAgreementId(internalServiceAgreementId)
                            .withFunctionGroupId(functionGroup.getFunctionAccessGroupId())
                            .withDataGroupIds(dataGroupIds))
                            .then()
                            .statusCode(SC_OK);

                    LOGGER.info(String.format("Permission assigned for service agreement [%s], user [%s], function group [%s], data groups %s", internalServiceAgreementId, externalUserId, functionGroup.getFunctionAccessGroupId(), dataGroupIds));
                });
    }
}