package com.backbase.ct.bbfuel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is the main configuration class.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties("bb-fuel")
public class BbFuelConfiguration {

    /**
     * Configuration of the platform (infra) services.
     */
    PlatformConfig platform;

    /**
     * Configuration of DBS capabilities.
     */
    DbsConfig dbs;

}
