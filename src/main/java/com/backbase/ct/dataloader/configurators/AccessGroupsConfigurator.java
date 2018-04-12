package com.backbase.ct.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.dto.ArrangementId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermission;
import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.createPermissionsWithAllPrivileges;
import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.generateDataGroupPostRequestBody;
import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.generateFunctionGroupPostRequestBody;
import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupsConfigurator.class);

    private AccessGroupIntegrationRestClient accessGroupIntegrationRestClient = new AccessGroupIntegrationRestClient();

    private Map<String, String> functionGroupsAllPrivilegesCache = Collections.synchronizedMap(new HashMap<>());

    private static final String ARRANGEMENTS = "ARRANGEMENTS";

    public String ingestFunctionGroupsWithAllPrivilegesByFunctionNames(String externalServiceAgreementId, List<String> functionNames) {
        List<FunctionsGetResponseBody> sepaCtFunctions = this.accessGroupIntegrationRestClient.retrieveFunctions(functionNames);

        return ingestFunctionGroupWithAllPrivileges(externalServiceAgreementId, sepaCtFunctions);
    }

    public String ingestFunctionGroupsWithAllPrivilegesNotContainingProvidedFunctionNames(String externalServiceAgreementId, List<String> functionNames) {
        List<FunctionsGetResponseBody> sepaCtFunctions = this.accessGroupIntegrationRestClient.retrieveFunctionsNotContainingProvidedFunctionNames(functionNames);

        return ingestFunctionGroupWithAllPrivileges(externalServiceAgreementId, sepaCtFunctions);
    }

    private synchronized String ingestFunctionGroupWithAllPrivileges(String externalServiceAgreementId, List<FunctionsGetResponseBody> functions) {
        String cacheKey = String.format("%s-%s", externalServiceAgreementId, functions);

        if (functionGroupsAllPrivilegesCache.containsKey(cacheKey)) {
            return functionGroupsAllPrivilegesCache.get(cacheKey);
        }

        String functionGroupId = ingestFunctionGroup(externalServiceAgreementId, createPermissionsWithAllPrivileges(functions));
        functionGroupsAllPrivilegesCache.put(cacheKey, functionGroupId);
        return functionGroupId;
    }

    private String ingestFunctionGroup(String externalServiceAgreementId, List<Permission> permissions) {
        String functionGroupId = accessGroupIntegrationRestClient.ingestFunctionGroup(generateFunctionGroupPostRequestBody(externalServiceAgreementId, permissions))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(FunctionGroupsPostResponseBody.class)
            .getId();

        LOGGER.info("Function group [{}] ingested (service agreement [{}]) with permissions {}", functionGroupId, externalServiceAgreementId, permissions);

        return functionGroupId;
    }

    public String ingestDataGroupForArrangements(String externalServiceAgreementId, List<ArrangementId> arrangementIds) {
        List<String> internalArrangementIds = arrangementIds.stream()
            .map(ArrangementId::getInternalArrangementId)
            .collect(Collectors.toList());

        String dataGroupId = accessGroupIntegrationRestClient.ingestDataGroup(generateDataGroupPostRequestBody(externalServiceAgreementId, ARRANGEMENTS, internalArrangementIds))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(DataGroupsPostResponseBody.class)
            .getId();

        LOGGER.info("Data group [{}] ingested (service agreement [{}]) for arrangements {}", dataGroupId, externalServiceAgreementId, internalArrangementIds);

        return dataGroupId;
    }
}