package com.tdd.secureflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SecureflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureflowApplication.class, args);
    }

}
