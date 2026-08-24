package com.fintechplatform.accounts.client;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** Thin wrapper around customer-service's HTTP API. Keeps the raw RestClient
 * calls out of the domain service so AccountService reads as business logic,
 * not plumbing. */
@Component
public class CustomerClient {

    private final RestClient restClient;

    public CustomerClient(@Qualifier("customerServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public boolean isApprovedCustomer(UUID customerId) {
        try {
            ApprovalStatus status = restClient.get()
                    .uri("/api/customers/{id}/approved", customerId)
                    .retrieve()
                    .body(ApprovalStatus.class);
            return status != null && status.approved();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private record ApprovalStatus(boolean approved) {}
}
