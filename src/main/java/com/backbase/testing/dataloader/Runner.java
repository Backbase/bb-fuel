package com.backbase.testing.dataloader;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.testing.dataloader.configurators.LegalEntitiesAndUsersConfigurator;
import com.backbase.testing.dataloader.configurators.ServiceAgreementsConfigurator;
import com.backbase.testing.dataloader.setup.BankSetup;
import com.backbase.testing.dataloader.setup.UsersSetup;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.testing.dataloader.utils.ParserUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_ROOT_LEGAL_ENTITY_ID;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_USERS_JSON_LOCATION;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_USERS_WITHOUT_PERMISSIONS;
import static com.backbase.testing.dataloader.data.CommonConstants.USERS_JSON_EXTERNAL_USER_IDS_FIELD;

public class Runner {

    public static void main(String[] args) throws IOException {
        GlobalProperties globalProperties = GlobalProperties.getInstance();
        BankSetup bankSetup = new BankSetup();
        UsersSetup usersSetup = new UsersSetup();
        LegalEntitiesAndUsersConfigurator legalEntitiesAndUsersConfigurator = new LegalEntitiesAndUsersConfigurator();
        ServiceAgreementsConfigurator serviceAgreementsConfigurator = new ServiceAgreementsConfigurator();
        List<HashMap<String, List<String>>> userLists = ParserUtil.convertJsonToObject(globalProperties.get(PROPERTY_USERS_JSON_LOCATION), new TypeReference<List<HashMap<String, List<String>>>>() {});
        List<HashMap<String, List<String>>> usersWithoutPermissionsLists = ParserUtil.convertJsonToObject(globalProperties.get(PROPERTY_USERS_WITHOUT_PERMISSIONS), new TypeReference<List<HashMap<String, List<String>>>>() {});
        ServiceAgreementPostRequestBody[] serviceAgreementPostRequestBodies = ParserUtil.convertJsonToObject(globalProperties.get(PROPERTY_SERVICEAGREEMENTS_JSON_LOCATION), ServiceAgreementPostRequestBody[].class);

        bankSetup.setupBankWithEntitlementsAdminAndProducts();

        for (Map<String, List<String>> userList : userLists) {
            List<String> externalUserIds = userList.get(USERS_JSON_EXTERNAL_USER_IDS_FIELD);

            usersSetup.setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(externalUserIds);
        }

        for (Map<String, List<String>> usersWithoutPermissionsList : usersWithoutPermissionsLists) {
            List<String> externalUserIds = usersWithoutPermissionsList.get(USERS_JSON_EXTERNAL_USER_IDS_FIELD);

            legalEntitiesAndUsersConfigurator.ingestUsersUnderNewLegalEntity(externalUserIds, EXTERNAL_ROOT_LEGAL_ENTITY_ID);
        }

        for (ServiceAgreementPostRequestBody serviceAgreementGetResponseBody : serviceAgreementPostRequestBodies) {
            serviceAgreementsConfigurator.ingestServiceAgreementWithProvidersAndConsumersWithAllFunctionDataGroups(serviceAgreementGetResponseBody.getProviders(), serviceAgreementGetResponseBody.getConsumers());
        }
    }
}
