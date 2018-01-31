package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.data.AccessGroupsDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupsConfigurator.class);

    private AccessGroupsDataGenerator accessGroupsDataGenerator = new AccessGroupsDataGenerator();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();

    public void ingestFunctionGroupsWithAllPrivilegesForAllFunctions(String externalLegalEntityId) {
        FunctionsGetResponseBody[] functions = accessGroupIntegrationRestClient.retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class);

        for (FunctionsGetResponseBody function : functions) {
            List<Privilege> functionPrivileges = function.getPrivileges();
            List<String> privileges = new ArrayList<>();

            functionPrivileges.forEach(fp -> privileges.add(fp.getPrivilege()));

            ingestFunctionGroupByFunctionName(externalLegalEntityId, function.getName(), privileges);
        }
    }

    public String ingestFunctionGroupsWithAllPrivilegesByFunctionName(String externalLegalEntityId, String functionName) {
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

        LOGGER.info(String.format("Function group ingested (legal entity [%s]) for function [%s] with privileges %s", externalLegalEntityId, functionName, privileges));

        return id;
    }

    public String ingestDataGroupForArrangements(String externalLegalEntityId, List<String> internalArrangementIds) {
        String id = accessGroupIntegrationRestClient.ingestDataGroup(accessGroupsDataGenerator.generateDataGroupsPostRequestBody(externalLegalEntityId, DataGroupsPostRequestBody.Type.ARRANGEMENTS, internalArrangementIds))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(DataGroupsPostResponseBody.class)
                .getId();

        LOGGER.info(String.format("Data group ingested (legal entity [%s]) for arrangements %s", externalLegalEntityId, internalArrangementIds));

        return id;
    }
}