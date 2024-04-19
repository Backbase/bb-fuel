package com.backbase.ct.bbfuel.config;

import com.backbase.ct.bbfuel.util.GlobalProperties;
import lombok.Getter;
import lombok.Setter;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MULTI_TENANCY_ENVIRONMENT;
import static com.backbase.ct.bbfuel.data.CommonConstants.TOPSTACK_EPHEMERAL_ENVIRONMENT;

/**
 * Config for multi-tenancy environments.
 * This is not yet Spring configured as future refactoring of data.properties and GlobalProperties will handle that.
 */
public class TopstackEphemeralConfig {

    public static boolean isTopstackEphemeralEnvironment() {
        return  GlobalProperties.getInstance().getBoolean(TOPSTACK_EPHEMERAL_ENVIRONMENT);
    }
}
