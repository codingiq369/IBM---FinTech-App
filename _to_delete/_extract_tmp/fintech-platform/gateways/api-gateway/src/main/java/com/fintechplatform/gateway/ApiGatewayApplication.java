package com.fintechplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API gateway.
 *
 * <p>The gateway itself has no business logic — its whole job is routing
 * (see application.yml) and CORS, so the web-banking demo UI has exactly
 * one host and port to talk to instead of needing to know where every
 * individual service lives.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
