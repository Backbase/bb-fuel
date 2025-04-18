package com.backbase.ct.bbfuel.data;

import static java.util.Arrays.asList;
import com.backbase.dbs.limit.client.api.v2.model.CreateLimitRequestBody;
import com.backbase.dbs.limit.client.api.v2.model.Entity;
import com.backbase.dbs.limit.client.api.v2.model.TransactionalLimitsBound;
import java.math.BigDecimal;

public class LimitsDataGenerator {

    private static final String SERVICE_AGREEMENT_ENTITY = "SA";
    private static final String FUNCTION_GROUP_ENTITY = "FAG";
    private static final String FUNCTION_ENTITY = "FUN";
    private static final String PRIVILEGE_ENTITY = "PRV";

    public static CreateLimitRequestBody createTransactionalLimitsPostRequestBodyForPrivilege(
        String internalServiceAgreementId,
        String functionGroupId,
        String functionId,
        String currency,
        String privilege,
        BigDecimal limitAmount) {
        return new CreateLimitRequestBody()
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
            .withTransactionalLimitsBound(new TransactionalLimitsBound()
                .withAmount(limitAmount))
            .withShadow(false);
    }

}
