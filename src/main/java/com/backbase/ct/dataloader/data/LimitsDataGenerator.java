package com.backbase.ct.dataloader.data;

import static java.util.Arrays.asList;

import com.backbase.presentation.limit.rest.spec.v2.limits.AmountRequiredTransactionalLimitsBound;
import com.backbase.presentation.limit.rest.spec.v2.limits.Entity;
import com.backbase.presentation.limit.rest.spec.v2.limits.TransactionalLimitsPostRequestBody;
import java.math.BigDecimal;

public class LimitsDataGenerator {

    private static final String SERVICE_AGREEMENT_ENTITY = "SA";
    private static final String FUNCTION_GROUP_ENTITY = "FAG";
    private static final String FUNCTION_ENTITY = "FUN";
    private static final String PRIVILEGE_ENTITY = "PRV";

    public static TransactionalLimitsPostRequestBody createTransactionalLimitsPostRequestBodyForPrivilege(
        String internalServiceAgreementId,
        String functionGroupId,
        String functionId,
        String currency,
        String privilege,
        BigDecimal limitAmount) {
        return new TransactionalLimitsPostRequestBody()
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
                    .withEref(privilege)))
            .withCurrency(currency)
            .withTransactionalLimitsBound(new AmountRequiredTransactionalLimitsBound()
                .withAmount(limitAmount))
            .withShadow(false);
    }

}
