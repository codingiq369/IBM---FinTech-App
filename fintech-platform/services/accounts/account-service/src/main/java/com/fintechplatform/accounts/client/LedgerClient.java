package com.fintechplatform.accounts.client;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Thin wrapper around ledger-service's HTTP API for the two things
 * accounts-service needs: opening a ledger account when a bank account is
 * opened, and reading a balance on demand. Posting money movements is
 * transfers-service's job, not this one's. */
@Component
public class LedgerClient {

    private final RestClient restClient;

    public LedgerClient(@Qualifier("ledgerServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public LedgerAccountResponse openLedgerAccount(UUID accountId, String currency) {
        return restClient.post()
                .uri("/api/ledger/accounts")
                .body(new OpenLedgerAccountRequest(accountId, currency))
                .retrieve()
                .body(LedgerAccountResponse.class);
    }

    public LedgerBalanceResponse getBalance(UUID ledgerAccountId) {
        return restClient.get()
                .uri("/api/ledger/accounts/{id}/balance", ledgerAccountId)
                .retrieve()
                .body(LedgerBalanceResponse.class);
    }
}
