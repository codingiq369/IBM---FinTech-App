package com.fintechplatform.transfers.client;

import com.fintechplatform.transfers.service.AccountNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class AccountsClient {

    private final RestClient restClient;

    public AccountsClient(@Qualifier("accountsServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public AccountResponse getAccount(UUID accountId) {
        try {
            return restClient.get().uri("/api/accounts/{id}", accountId).retrieve().body(AccountResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException(accountId);
        }
    }
}
