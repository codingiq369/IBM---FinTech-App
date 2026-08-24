package com.fintechplatform.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the customer-service.
 *
 * <p>This service is the system of record for "who is this person". Every other
 * service that needs to know a customer exists (accounts, compliance, etc.) asks
 * this service rather than keeping its own copy of customer data — that's the
 * core microservices idea of each service owning one slice of the business and
 * being the single source of truth for it.
 */
@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
