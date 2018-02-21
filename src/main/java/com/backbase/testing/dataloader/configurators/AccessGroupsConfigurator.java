package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.dto.ArrangementId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.backbase.testing.dataloader.data.AccessGroupsDataGenerator.generateDataGroupPostRequestBody;
import static com.backbase.testing.dataloader.data.AccessGroupsDataGenerator.generateFunctionGroupPostRequestBody;
import static org.apache.http.HttpStatus.SC_CREATED;

public class AccessGroupsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupsConfigurator.class);

    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();
    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();

    private Map<String, String> allPrivilegesCache = Collections.synchronizedMap(new HashMap<>());

    private Map<String, String> functionGroupsAllPrivilegesCache = Collections.synchronizedMap(new HashMap<>());

    public String setupFunctionGroupWithAllPrivilegesByFunctionName(String internalServiceAgreementId, String externalServiceAgreementId, String functionName) {

        String cacheKey = String.format("%s-%s-%s", internalServiceAgreementId, externalServiceAgreementId, functionName);
        if (allPrivilegesCache.containsKey(cacheKey)) {
            return allPrivilegesCache.get(cacheKey);
        }
        String functionGroupId = accessGroupPresentationRestClient.getFunctionGroupIdByServiceAgreementIdAndFunctionName(internalServiceAgreementId, functionName);

        if (functionGroupId == null) {
            functionGroupId = ingestFunctionGroupWithAllPrivilegesByFunctionName(externalServiceAgreementId, functionName);
        }
        allPrivilegesCache.put(cacheKey, functionGroupId);
        return functionGroupId;
    }

    public String ingestFunctionGroupWithAllPrivilegesByFunctionName(String externalServiceAgreementId, String functionName) {
        String cacheKey = String.format("%s-%s", externalServiceAgreementId, functionName);

        if (functionGroupsAllPrivilegesCache.containsKey(cacheKey)) {
            return functionGroupsAllPrivilegesCache.get(cacheKey);
        }

        List<String> privileges = new ArrayList<>();
        FunctionsGetResponseBody function = accessGroupIntegrationRestClient.retrieveFunctionByName(functionName);
        List<Privilege> functionPrivileges = function.getPrivileges();

        functionPrivileges.forEach(fp -> privileges.add(fp.getPrivilege()));

        String functionId = ingestFunctionGroupByFunctionName(externalServiceAgreementId, function.getName(), privileges);
        functionGroupsAllPrivilegesCache.put(cacheKey, functionId);
        return functionId;
    }

    private String ingestFunctionGroupByFunctionName(String externalServiceAgreementId, String functionName, List<String> privileges) {
        String functionId = accessGroupIntegrationRestClient.retrieveFunctionByName(functionName)
                .getFunctionId();

        String id = accessGroupIntegrationRestClient.ingestFunctionGroup(generateFunctionGroupPostRequestBody(externalServiceAgreementId, functionId, privileges))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(FunctionGroupsPostResponseBody.class)
                .getId();

        LOGGER.info(String.format("Function group [%s] ingested (service agreement [%s]) for function [%s] with privileges %s", id, externalServiceAgreementId, functionName, privileges));

        return id;
    }

    public String ingestDataGroupForArrangements(String externalServiceAgreementId, List<ArrangementId> arrangementIds) {
        List<String> internalArrangementIds = arrangementIds.stream().map(ArrangementId::getInternalArrangementId).collect(Collectors.toList());

        String id = accessGroupIntegrationRestClient.ingestDataGroup(generateDataGroupPostRequestBody(externalServiceAgreementId, DataGroupPostRequestBody.Type.ARRANGEMENTS, internalArrangementIds))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(DataGroupsPostResponseBody.class)
                .getId();

        LOGGER.info(String.format("Data group [%s] ingested (service agreement [%s]) for arrangements %s", id, externalServiceAgreementId, internalArrangementIds));

        return id;
    }
}