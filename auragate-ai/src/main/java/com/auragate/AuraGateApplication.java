package com.auragate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.auragate")
@EnableConfigurationProperties
public class AuraGateApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuraGateApplication.class, args);
    }
}
