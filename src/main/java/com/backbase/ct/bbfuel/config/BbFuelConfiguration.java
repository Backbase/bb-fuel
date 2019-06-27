package com.backbase.ct.bbfuel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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

    /**
     * Spring Boot does not automatically define a RestTemplate and BB codeGen generated clients require this.
     *
     * @param builder configurable builder to build a RestTemplate
     * @return a RestTemplate
     */
    @Bean("interServiceRestTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}
