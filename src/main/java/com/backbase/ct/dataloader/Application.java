package com.backbase.ct.dataloader;

import com.backbase.buildingblocks.backend.configuration.autoconfigure.BackbaseApplication;
import java.io.IOException;
import org.springframework.boot.SpringApplication;

@BackbaseApplication
public class Application {

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(Runner.class);
        application.setWebEnvironment(false);
        application.run(args);
        try {
            Runner.doit(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
