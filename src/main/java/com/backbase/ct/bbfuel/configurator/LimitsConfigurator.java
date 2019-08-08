package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.ACH_DEBIT_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.ct.bbfuel.data.LimitsDataGenerator.createTransactionalLimitsPostRequestBodyForPrivilege;
import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.limit.LimitsPresentationRestClient;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LimitsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitsConfigurator.class);
    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final LimitsPresentationRestClient limitsPresentationRestClient;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
    private static final List<String> PRIVILEGES = asList("create", "approve");
    private static final String ADMIN_FUNCTION_GROUP_NAME = "Admin";

    public void ingestLimits(String internalServiceAgreementId) {
        BigDecimal limitAmount = new BigDecimal("1000000.0");

        loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String externalServiceAgreementId = serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();

        List<FunctionsGetResponseBody> paymentsFunctions = accessGroupIntegrationRestClient
            .retrieveFunctions(asList(
                SEPA_CT_FUNCTION_NAME,
                US_DOMESTIC_WIRE_FUNCTION_NAME,
                US_FOREIGN_WIRE_FUNCTION_NAME,
                ACH_DEBIT_FUNCTION_NAME));

        for (FunctionsGetResponseBody paymentsFunction : paymentsFunctions) {
            String currency = SEPA_CT_FUNCTION_NAME.equals(paymentsFunction.getName()) ? "EUR" : "USD";

            String existingAdminFunctionGroupId = accessGroupPresentationRestClient
                .retrieveFunctionGroupsByServiceAgreement(internalServiceAgreementId)
                .stream()
                .filter(functionGroupsGetResponseBody -> ADMIN_FUNCTION_GROUP_NAME
                    .equals(functionGroupsGetResponseBody.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String
                    .format("No existing function group found by service agreement [%s] and name [%s]",
                        externalServiceAgreementId, ADMIN_FUNCTION_GROUP_NAME)))
                .getId();

            for (String privilege : PRIVILEGES) {
                String limitId = limitsPresentationRestClient.createTransactionalLimit(
                    createTransactionalLimitsPostRequestBodyForPrivilege(
                        internalServiceAgreementId,
                        existingAdminFunctionGroupId,
                        paymentsFunction.getFunctionId(),
                        currency,
                        privilege,
                        limitAmount))
                    .then()
                    .statusCode(SC_CREATED)
                    .extract()
                    .path("uuid");

                LOGGER.info("Transactional limit [{}] created for {} privilege on function group {} and function {}",
                    limitId, privilege, existingAdminFunctionGroupId, paymentsFunction.getFunctionId());
            }
        }
    }
}
