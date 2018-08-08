package com.backbase.ct.dataloader.data;

import static java.util.Arrays.asList;

import com.backbase.presentation.limit.rest.spec.v2.limits.Entity;
import com.backbase.presentation.limit.rest.spec.v2.limits.PeriodicLimitsBounds;
import com.backbase.presentation.limit.rest.spec.v2.limits.PeriodicLimitsPostRequestBody;
import java.math.BigDecimal;

public class LimitsDataGenerator {

    private static final String SERVICE_AGREEMENT_ENTITY = "SA";
    private static final String FUNCTION_GROUP_ENTITY = "FAG";
    private static final String FUNCTION_ENTITY = "FUN";
    private static final String PRIVILEGE_ENTITY = "PRV";
    private static final String PRIVILEGE_APPROVE = "approve";

    public static PeriodicLimitsPostRequestBody createDailyLimitsPostRequestBodyForApprovePrivilege(
        String internalServiceAgreementId,
        String functionGroupId,
        String functionId,
        String currency,
        BigDecimal limitAmount) {
        return new PeriodicLimitsPostRequestBody()
            .withUserBBID(null)
            .withEntities(asList(
                new Entity()
                    .withEtype(SERVICE_AGREEMENT_ENTITY)
                    .withEref(internalServiceAgreementId),
                new Entity()
                    .withEtype(FUNCTION_GROUP_ENTITY)
                    .withEref(functionGroupId),
                new Entity()
                    .withEtype(FUNCTION_ENTITY)
                    .withEref(functionId),
                new Entity()
                    .withEtype(PRIVILEGE_ENTITY)
                    .withEref(PRIVILEGE_APPROVE)))
            .withCurrency(currency)
            .withPeriodicLimitsBounds(new PeriodicLimitsBounds()
                .withDaily(limitAmount))
            .withShadow(false);
    }

}
