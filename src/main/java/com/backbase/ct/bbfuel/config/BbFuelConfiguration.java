package com.backbase.ct.bbfuel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("bb-fuel")
public class BbFuelConfiguration {

    PlatformConfig platform;

    DbsConfig dbs;

}
