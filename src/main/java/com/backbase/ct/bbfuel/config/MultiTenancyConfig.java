package com.backbase.ct.bbfuel.config;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MULTI_TENANCY_ENVIRONMENT;

import com.backbase.ct.bbfuel.util.GlobalProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Config for multi-tenancy environments.
 * This is not yet Spring configured as future refactoring of data.properties and GlobalProperties will handle that.
 */
public class MultiTenancyConfig {

    @Getter
    @Setter
    private static String tenantId;

    public static boolean isMultiTenancyEnvironment() {
        return  GlobalProperties.getInstance().getBoolean(PROPERTY_MULTI_TENANCY_ENVIRONMENT);
    }
}
