package com.backbase.ct.bbfuel.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Holding configuration for platform services.
 */
@Getter
@Setter
public class PlatformConfig {

    /**
     * URI to the registry service.
     */
    private String registry;

    /**
     * URI to the gateway service.
     */
    private String gateway;

    /**
     * URI to the auth service.
     */
    private String auth;

}
