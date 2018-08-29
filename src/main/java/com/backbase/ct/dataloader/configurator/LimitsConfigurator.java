package com.backbase.ct.dataloader.configurator;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.dataloader.data.CommonConstants.SEPA_CT_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_DOMESTIC_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.CommonConstants.US_FOREIGN_WIRE_FUNCTION_NAME;
import static com.backbase.ct.dataloader.data.LimitsDataGenerator.createDailyLimitsPostRequestBodyForApprovePrivilege;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.client.accessgroup.AccessGroupIntegrationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.ct.dataloader.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.client.limit.LimitsPresentationRestClient;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
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

    private LoginRestClient loginRestClient = new LoginRestClient();
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private final AccessGroupPresentationRestClient accessGroupPresentationRestClient;
    private final AccessGroupIntegrationRestClient accessGroupIntegrationRestClient;
    private final LimitsPresentationRestClient limitsPresentationRestClient;
    private String bankAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);

    public void ingestLimits(String internalServiceAgreementId) {
        BigDecimal limitAmount = new BigDecimal("1000000.0");

        loginRestClient.login(bankAdmin, bankAdmin);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        List<FunctionsGetResponseBody> paymentsFunctions = accessGroupIntegrationRestClient
            .retrieveFunctions(asList(
                SEPA_CT_FUNCTION_NAME,
                US_DOMESTIC_WIRE_FUNCTION_NAME,
                US_FOREIGN_WIRE_FUNCTION_NAME));

        for (FunctionsGetResponseBody paymentsFunction : paymentsFunctions) {
            String currency = SEPA_CT_FUNCTION_NAME.equals(paymentsFunction.getName()) ? "EUR" : "USD";

            List<String> paymentsFunctionGroupIds = accessGroupPresentationRestClient
                .getFunctionGroupsByFunctionId(internalServiceAgreementId, paymentsFunction.getFunctionId())
                .stream()
                .map(FunctionGroupsGetResponseBody::getId)
                .collect(toList());

            paymentsFunctionGroupIds.forEach(paymentsFunctionGroupId -> {
                String limitId = limitsPresentationRestClient.createPeriodicLimit(createDailyLimitsPostRequestBodyForApprovePrivilege(
                    internalServiceAgreementId,
                    paymentsFunctionGroupId,
                    paymentsFunction.getFunctionId(),
                    currency,
                    limitAmount))
                    .then()
                    .statusCode(SC_CREATED)
                .extract()
                .path("uuid");

                LOGGER.info("Daily limit [{}] created for approve privilege on function group {} and function {}",
                    limitId, paymentsFunctionGroupId, paymentsFunction.getFunctionId());
            });

        }
    }
}
