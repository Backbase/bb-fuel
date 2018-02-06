package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.users.permissions.AssignPermissionsPostRequestBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.data.AccessGroupsDataGenerator;
import com.backbase.testing.dataloader.dto.ArrangementId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupsConfigurator.class);

    private AccessGroupsDataGenerator accessGroupsDataGenerator = new AccessGroupsDataGenerator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();

    public void setupFunctionDataGroupAndAllPrivilegesAssignedToUserAndMasterServiceAgreement(LegalEntityByUserGetResponseBody legalEntity, String externalUserId, String functionName, List<String> dataGroupIds) {
        String functionGroupId = accessGroupPresentationRestClient.getFunctionGroupIdByLegalEntityIdAndFunctionName(legalEntity.getId(), functionName);

        if (functionGroupId == null) {
            functionGroupId = ingestFunctionGroupsWithAllPrivilegesByFunctionName(legalEntity.getExternalId(), functionName);
        }

        accessGroupIntegrationRestClient.assignPermissions(new AssignPermissionsPostRequestBody()
                .withExternalLegalEntityId(legalEntity.getExternalId())
                .withExternalUserId(externalUserId)
                .withServiceAgreementId(null)
                .withFunctionGroupId(functionGroupId)
                .withDataGroupIds(dataGroupIds))
                .then()
                .statusCode(SC_OK);

        LOGGER.info(String.format("Permission assigned for legal entity [%s], user [%s], service agreement [master], function group [%s], data groups %s", legalEntity.getExternalId(), externalUserId, functionGroupId, dataGroupIds));
    }

    public void ingestFunctionGroupsWithAllPrivilegesForAllFunctions(String externalLegalEntityId) {
        FunctionsGetResponseBody[] functions = accessGroupIntegrationRestClient.retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class);

        Arrays.stream(functions).parallel().forEach(function -> {
            List<Privilege> functionPrivileges = function.getPrivileges();
            List<String> privileges = new ArrayList<>();

            functionPrivileges.forEach(fp -> privileges.add(fp.getPrivilege()));

            ingestFunctionGroupByFunctionName(externalLegalEntityId, function.getName(), privileges);
        });
    }

    private String ingestFunctionGroupsWithAllPrivilegesByFunctionName(String externalLegalEntityId, String functionName) {
        List<String> privileges = new ArrayList<>();
        FunctionsGetResponseBody function = accessGroupIntegrationRestClient.retrieveFunctionByName(functionName);
        List<Privilege> functionPrivileges = function.getPrivileges();

        functionPrivileges.forEach(fp -> privileges.add(fp.getPrivilege()));

        return ingestFunctionGroupByFunctionName(externalLegalEntityId, function.getName(), privileges);
    }

    private String ingestFunctionGroupByFunctionName(String externalLegalEntityId, String functionName, List<String> privileges) {
        String functionId = accessGroupIntegrationRestClient.retrieveFunctionByName(functionName)
                .getFunctionId();

        String id = accessGroupIntegrationRestClient.ingestFunctionGroup(accessGroupsDataGenerator.generateFunctionGroupsPostRequestBody(externalLegalEntityId, functionId, privileges))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(FunctionGroupsPostResponseBody.class)
                .getId();

        LOGGER.info(String.format("Function group [%s] ingested (legal entity [%s]) for function [%s] with privileges %s", id, externalLegalEntityId, functionName, privileges));

        return id;
    }

    public String ingestDataGroupForArrangements(String externalLegalEntityId, List<ArrangementId> arrangementIds) {
        List<String> internalArrangementIds = arrangementIds.stream().map(ArrangementId::getInternalArrangementId).collect(Collectors.toList());

        String id = accessGroupIntegrationRestClient.ingestDataGroup(accessGroupsDataGenerator.generateDataGroupsPostRequestBody(externalLegalEntityId, DataGroupsPostRequestBody.Type.ARRANGEMENTS, internalArrangementIds))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(DataGroupsPostResponseBody.class)
                .getId();

        LOGGER.info(String.format("Data group [%s] ingested (legal entity [%s]) for arrangements %s", id, externalLegalEntityId, internalArrangementIds));

        return id;
    }
}