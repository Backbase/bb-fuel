package com.backbase.ct.bbfuel;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.backbase.ct.bbfuel",
    "com.backbase.presentation",
    "com.backbase.integration"})
public class BbFuelApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BbFuelApplication.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
