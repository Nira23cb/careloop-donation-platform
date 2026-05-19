package com.careloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the CareLoop Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class CareLoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareLoopApplication.class, args);
    }
}
