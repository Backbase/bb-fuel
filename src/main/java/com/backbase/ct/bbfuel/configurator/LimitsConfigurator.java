package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.LimitsDataGenerator.createTransactionalLimitsPostRequestBodyForPrivilege;
import static com.backbase.ct.bbfuel.service.PaymentsFunctionService.BATCH_FUNCTIONS;
import static com.backbase.ct.bbfuel.service.PaymentsFunctionService.PAYMENTS_FUNCTIONS;
import static com.backbase.ct.bbfuel.service.PaymentsFunctionService.determineCurrencyForFunction;
import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.limit.LimitsPresentationRestClient;
import com.backbase.dbs.accesscontrol.ac_permission_set.integration.v1.model.PermissionItem;
import com.backbase.dbs.accesscontrol.client.v3.model.FunctionGroupItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitsConfigurator {

    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;
    private final LimitsPresentationRestClient limitsPresentationRestClient;
    private static final List<String> PRIVILEGES = asList("create", "approve");
    private static final String ADMIN_FUNCTION_GROUP_NAME = "Admin";

    public void ingestLimits(String internalServiceAgreementId) {
        BigDecimal limitAmount = new BigDecimal("1000000.0");

        loginRestClient.loginBankAdmin();
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String externalServiceAgreementId = serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();

        List<PermissionItem> paymentsFunctions = accessGroupIntegrationRestClient
            .retrieveFunctions(Stream.concat(
                    PAYMENTS_FUNCTIONS.stream(), BATCH_FUNCTIONS.stream())
                .collect(Collectors.toList()));
        List<FunctionGroupItem> functionGroupItems = accessGroupPresentationRestClient
            .retrieveFunctionGroupsByServiceAgreement(internalServiceAgreementId);

        String existingAdminFunctionGroupId = functionGroupItems
            .stream()
            .filter(functionGroupsGetResponseBody -> ADMIN_FUNCTION_GROUP_NAME
                .equals(functionGroupsGetResponseBody.getName()))
            .map(FunctionGroupItem::getId)
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                String.format("No existing function group found by service agreement [%s] and name [%s]",
                    externalServiceAgreementId, ADMIN_FUNCTION_GROUP_NAME)));

        for (PermissionItem paymentsFunction : paymentsFunctions) {
            String paymentsFunctionId = functionGroupItems.stream()
                .filter(item -> item.getName().equals(paymentsFunction.getBusinessFunctionName()))
                .map(FunctionGroupItem::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                    String.format("No existing function group found by service agreement [%s] and name [%s]",
                        externalServiceAgreementId, paymentsFunction.getBusinessFunctionName())));

            for (String privilege : PRIVILEGES) {
                String limitId = limitsPresentationRestClient.createTransactionalLimit(
                        createTransactionalLimitsPostRequestBodyForPrivilege(
                            internalServiceAgreementId,
                            existingAdminFunctionGroupId,
                            paymentsFunctionId,
                            determineCurrencyForFunction(paymentsFunction.getBusinessFunctionName()),
                            privilege,
                            limitAmount))
                    .then()
                    .statusCode(SC_CREATED)
                    .extract()
                    .path("uuid");

                log.info("Transactional limit [{}] created for {} privilege on function group {} and function {}",
                    limitId, privilege, existingAdminFunctionGroupId, paymentsFunctionId);
            }
        }
    }
}
