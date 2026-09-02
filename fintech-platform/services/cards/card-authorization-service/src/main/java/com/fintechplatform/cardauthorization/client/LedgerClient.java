package com.fintechplatform.cardauthorization.client;

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

    /** Opens a new ledger account to act as the card network's clearing
     * account for one currency. Called at most once per currency — see
     * {@link com.fintechplatform.cardauthorization.service.ClearingAccountService}. */
    public LedgerAccountResponse openClearingLedgerAccount(String currency) {
        return restClient.post()
                .uri("/api/ledger/accounts")
                .body(new OpenLedgerAccountRequest("CARD_NETWORK_CLEARING:" + currency, currency))
                .retrieve()
                .body(LedgerAccountResponse.class);
    }

    /**
     * Posts the two-legged journal entry that makes an approved purchase
     * real: a debit against the cardholder's ledger account, a credit
     * against the card network's clearing account, for the same amount.
     * Any failure here — insufficient funds, or ledger-service being
     * unreachable — propagates as an exception for the caller to turn into
     * a DECLINED authorization.
     */
    public JournalEntryResponse postCardPurchase(
            UUID authorizationId, UUID cardholderLedgerAccountId, UUID clearingLedgerAccountId, BigDecimal amount, String description) {
        PostJournalEntryRequest request = new PostJournalEntryRequest(
                description,
                authorizationId.toString(),
                List.of(
                        new PostingRequest(cardholderLedgerAccountId, "DEBIT", amount),
                        new PostingRequest(clearingLedgerAccountId, "CREDIT", amount)));

        return restClient.post()
                .uri("/api/ledger/journal-entries")
                .body(request)
                .retrieve()
                .body(JournalEntryResponse.class);
    }
}
