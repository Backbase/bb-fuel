package com.backbase.ct.bbfuel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.backbase.ct.bbfuel",
    "com.backbase.presentation",
    "com.backbase.integration"})
public class BbFuelApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BbFuelApplication.class);
        application.run(args);
    }
}
