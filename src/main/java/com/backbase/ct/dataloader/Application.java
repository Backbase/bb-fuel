package com.backbase.ct.dataloader;

import com.backbase.buildingblocks.jwt.internal.config.EnableInternalJwtConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableInternalJwtConsumer
@ComponentScan(basePackages = {
    "com.backbase.ct.dataloader",
    "com.backbase.presentation",
    "com.backbase.integration"})
public class Application {

    public static void main(String[] args) {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebEnvironment(false);
        application.run(args);
    }
}
