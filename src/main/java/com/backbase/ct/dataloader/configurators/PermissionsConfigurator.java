package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.dto.CurrencyDataGroup;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

public class PermissionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsConfigurator.class);

    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();

    public void assignAllFunctionDataGroupsToUserAndServiceAgreement(String externalUserId, String internalServiceAgreementId) {
        List<String> functionGroupIds = accessGroupPresentationRestClient.retrieveFunctionGroupIdsByServiceAgreement(internalServiceAgreementId);
        List<String> dataGroupIds = accessGroupPresentationRestClient.retrieveDataGroupIdsByServiceAgreement(internalServiceAgreementId);

        functionGroupIds.forEach(functionGroupId -> {
            accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
                .withExternalLegalEntityId(null)
                .withExternalUserId(externalUserId)
                .withServiceAgreementId(internalServiceAgreementId)
                .withFunctionGroupId(functionGroupId)
                .withDataGroupIds(dataGroupIds))
                .then()
                .statusCode(SC_OK);

            LOGGER.info(String.format("Permission assigned for service agreement [%s], user [%s], function group [%s], data groups %s", internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds));
        });
    }

    public void assignPermissions(String externalUserId, String internalServiceAgreementId, String functionGroupId, List<String> dataGroupIds) {
        accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
            .withExternalLegalEntityId(null)
            .withExternalUserId(externalUserId)
            .withServiceAgreementId(internalServiceAgreementId)
            .withFunctionGroupId(functionGroupId)
            .withDataGroupIds(dataGroupIds))
            .then()
            .statusCode(SC_OK);

        LOGGER.info(String.format("Permission assigned for service agreement [%s], user [%s], function group [%s], data groups %s", internalServiceAgreementId, externalUserId, functionGroupId, dataGroupIds));
    }

    public void assignPermissions(String externalUserId, String internalServiceAgreementId, String functionName, String functionGroupId, CurrencyDataGroup currencyDataGroup) {
        switch (functionName) {
            case CommonConstants.SEPA_CT_FUNCTION_NAME:
                assignPermissions(externalUserId, internalServiceAgreementId, functionGroupId, singletonList(currencyDataGroup.getInternalEurCurrencyDataGroupId()));
                break;
            case CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME:
            case CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME:
                assignPermissions(externalUserId, internalServiceAgreementId, functionGroupId, singletonList(currencyDataGroup.getInternalUsdCurrencyDataGroupId()));
                break;
            default:
                assignPermissions(externalUserId, internalServiceAgreementId, functionGroupId, asList(currencyDataGroup.getInternalRandomCurrencyDataGroupId(), currencyDataGroup.getInternalEurCurrencyDataGroupId(), currencyDataGroup.getInternalUsdCurrencyDataGroupId()));
        }
    }
}