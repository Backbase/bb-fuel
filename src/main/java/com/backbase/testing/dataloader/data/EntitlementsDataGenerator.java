package com.backbase.testing.dataloader.data;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.data.DataGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.FunctionGroupsPostRequestBody;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.integration.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.backbase.integration.user.rest.spec.v2.users.UsersPostRequestBody;
import com.github.javafaker.Faker;
import org.apache.commons.lang.RandomStringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.backbase.testing.dataloader.data.CommonConstants.EXTERNAL_LEGAL_ENTITY_ID_PREFIX;

public class EntitlementsDataGenerator {

    private Faker faker = new Faker();

    public String generateExternalLegalEntityId() {
        return EXTERNAL_LEGAL_ENTITY_ID_PREFIX + RandomStringUtils.randomNumeric(8);
    }

    public LegalEntitiesPostRequestBody generateRootLegalEntitiesPostRequestBody(String externalLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
                .withExternalId(externalLegalEntityId)
                .withName(faker.company()
                        .name())
                .withParentExternalId(null)
                .withType(LegalEntityType.BANK);
    }

    public LegalEntitiesPostRequestBody generateLegalEntitiesPostRequestBody(String externalLegalEntityId, String externalParentLegalEntityId) {
        return new LegalEntitiesPostRequestBody()
                .withExternalId(externalLegalEntityId)
                .withName(faker.company()
                        .name())
                .withParentExternalId(externalParentLegalEntityId)
                .withType(LegalEntityType.CUSTOMER);
    }

    public UsersPostRequestBody generateUsersPostRequestBody(String externalUserId, String externalLegalEntityId) {
        return new UsersPostRequestBody()
                .withExternalId(externalUserId)
                .withLegalEntityExternalId(externalLegalEntityId)
                .withFullName(faker.name().fullName());
    }

    public FunctionGroupsPostRequestBody generateFunctionGroupsPostRequestBody(String externalLegalEntityId, String functionId, List<String> privileges) {
        return new FunctionGroupsPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalLegalEntityId(externalLegalEntityId)
                .withPermissions(setPermissions(functionId, privileges));
    }

    public DataGroupsPostRequestBody generateDataGroupsPostRequestBody(String externalLegalEntityId, DataGroupsPostRequestBody.Type type, List<String> items) {
        return new DataGroupsPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalLegalEntityId(externalLegalEntityId)
                .withType(type)
                .withItems(items);
    }

    private List<Permission> setPermissions(String function, List<String> privileges) {
        return Collections.singletonList(createPermission(function, privileges.toArray(new String[privileges.size()])));
    }

    private Permission createPermission(String function, String... privileges) {
        return new Permission()
                .withFunctionId(function)
                .withAssignedPrivileges(Arrays.asList(privileges)
                        .stream()
                        .map(privilege -> new Privilege().withPrivilege(privilege))
                        .collect(Collectors.toList()));
    }

    public ServiceAgreementPostRequestBody generateServiceAgreementPostRequestBody(String externalSaId, Set<Provider> providers, Set<Consumer> consumers) {
        return new ServiceAgreementPostRequestBody()
                .withName(faker.lorem().characters(8))
                .withDescription(faker.lorem().characters(8))
                .withExternalId(externalSaId)
                .withProviders(providers)
                .withConsumers(consumers);
    }
}
