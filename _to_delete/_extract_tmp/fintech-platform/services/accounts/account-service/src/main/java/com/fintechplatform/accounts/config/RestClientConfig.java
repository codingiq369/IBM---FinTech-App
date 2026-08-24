package com.fintechplatform.accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Base URLs for the other services this one talks to over plain HTTP. In a
 * bigger deployment these would come from service discovery (Eureka,
 * Kubernetes DNS, etc); for this slice, environment variables with
 * docker-compose service names are enough to show the pattern.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient customerServiceClient(@Value("${services.customer-service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient ledgerServiceClient(@Value("${services.ledger-service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
