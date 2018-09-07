package com.backbase.ct.dataloader.service;

import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.generateDataGroupPostRequestBody;
import static com.backbase.ct.dataloader.data.AccessGroupsDataGenerator.generateFunctionGroupPostRequestBody;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupPostResponseBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.exceptions.BadRequestException;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import io.restassured.response.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessGroupService {

    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;

    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;

    private final ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient;

    public String ingestFunctionGroup(String externalServiceAgreementId, String functionGroupName, List<Permission> permissions) {
        Response response = accessGroupIntegrationRestClient.ingestFunctionGroup(
            generateFunctionGroupPostRequestBody(externalServiceAgreementId, functionGroupName, permissions));

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrorCode()
                .equals("functionGroup.save.error.message.E_ALREADY_EXISTS")) {

            String internalServiceAgreementId = serviceAgreementsIntegrationRestClient
                .retrieveServiceAgreementByExternalId(externalServiceAgreementId)
                .getId();

            FunctionGroupsGetResponseBody existingFunctionGroup = accessGroupPresentationRestClient
                .retrieveFunctionGroupsByServiceAgreement(internalServiceAgreementId)
                .stream()
                .filter(functionGroupsGetResponseBody -> functionGroupName.equals(functionGroupsGetResponseBody.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existing function group found by service agreement and name"));

            return existingFunctionGroup.getId();

        } else {
            return response.then()
                .statusCode(SC_CREATED)
                .extract()
                .as(DataGroupPostResponseBody.class)
                .getId();
        }
    }

    public String ingestDataGroup(String externalServiceAgreementId, String dataGroupName,
        String type, List<String> internalArrangementIds) {
        Response response = accessGroupIntegrationRestClient.ingestDataGroup(
            generateDataGroupPostRequestBody(externalServiceAgreementId, dataGroupName, type, internalArrangementIds));

        if (response.statusCode() == SC_BAD_REQUEST &&
            response.then()
                .extract()
                .as(BadRequestException.class)
                .getErrorCode()
                .equals("dataAccessGroup.save.error.message.E_ALREADY_EXISTS")) {

            String internalServiceAgreementId = serviceAgreementsIntegrationRestClient
                .retrieveServiceAgreementByExternalId(externalServiceAgreementId)
                .getId();

            DataGroupsGetResponseBody existingDataGroup = accessGroupPresentationRestClient
                .retrieveDataGroupsByServiceAgreement(internalServiceAgreementId)
                .stream()
                .filter(dataGroupsGetResponseBody -> dataGroupName.equals(dataGroupsGetResponseBody.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existing data group found by service agreement and name"));

            return existingDataGroup.getId();

        } else {
            return response.then()
                .statusCode(SC_CREATED)
                .extract()
                .as(DataGroupPostResponseBody.class)
                .getId();
        }
    }

}
