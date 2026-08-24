package com.fintechplatform.transfers.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LedgerClient {

    private final RestClient restClient;

    public LedgerClient(@Qualifier("ledgerServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Posts the two-legged journal entry that makes a transfer real: a debit
     * against the source's ledger account, a credit against the
     * destination's, for the same amount. Any failure here — ledger-service
     * rejecting it (insufficient funds) or being unreachable — propagates
     * as an exception for the caller to turn into a FAILED transfer.
     */
    public JournalEntryResponse postTransfer(UUID transferId, UUID sourceLedgerAccountId, UUID destinationLedgerAccountId, BigDecimal amount, String description) {
        PostJournalEntryRequest request = new PostJournalEntryRequest(
                description,
                transferId.toString(),
                List.of(
                        new PostingRequest(sourceLedgerAccountId, "DEBIT", amount),
                        new PostingRequest(destinationLedgerAccountId, "CREDIT", amount)));

        return restClient.post()
                .uri("/api/ledger/journal-entries")
                .body(request)
                .retrieve()
                .body(JournalEntryResponse.class);
    }
}
