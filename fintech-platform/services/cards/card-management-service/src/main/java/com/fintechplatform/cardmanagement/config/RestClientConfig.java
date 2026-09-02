package com.fintechplatform.cardmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Base URL for the one other service this one talks to over plain HTTP. Same
 * pattern as every other service in this slice: environment variables with
 * docker-compose service names stand in for real service discovery.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient accountsServiceClient(@Value("${services.accounts-service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
