package com.fintechplatform.cardauthorization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.cardauthorization.client.JournalEntryResponse;
import com.fintechplatform.cardauthorization.client.LedgerClient;
import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import com.fintechplatform.cardauthorization.domain.CardAuthorizationStatus;
import com.fintechplatform.cardauthorization.repository.CardAuthorizationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class CardAuthorizationExecutionServiceTest {

    @Mock
    private CardAuthorizationRepository cardAuthorizationRepository;

    @Mock
    private LedgerClient ledgerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void aSuccessfulLedgerPostingApprovesTheAuthorization() {
        CardAuthorizationExecutionService service = new CardAuthorizationExecutionService(cardAuthorizationRepository, ledgerClient, objectMapper);
        CardAuthorization authorization = CardAuthorization.pendingLedgerDecision(
                UUID.randomUUID(), UUID.randomUUID(), "Coffee Shop", new BigDecimal("4.50"), "USD");
        UUID journalEntryId = UUID.randomUUID();

        when(ledgerClient.postCardPurchase(any(), any(), any(), any(), any()))
                .thenReturn(new JournalEntryResponse(journalEntryId, "Card purchase", authorization.getCardId().toString(), Instant.now()));
        when(cardAuthorizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardAuthorization result = service.execute(authorization, UUID.randomUUID(), UUID.randomUUID());

        assertThat(result.getStatus()).isEqualTo(CardAuthorizationStatus.APPROVED);
        assertThat(result.getJournalEntryReference()).isEqualTo(journalEntryId);
    }

    @Test
    void aLedgerFailureDeclinesTheAuthorizationInsteadOfThrowing() {
        CardAuthorizationExecutionService service = new CardAuthorizationExecutionService(cardAuthorizationRepository, ledgerClient, objectMapper);
        CardAuthorization authorization = CardAuthorization.pendingLedgerDecision(
                UUID.randomUUID(), UUID.randomUUID(), "Electronics Store", new BigDecimal("9999.00"), "USD");

        when(ledgerClient.postCardPurchase(any(), any(), any(), any(), any())).thenThrow(new RestClientException("insufficient funds"));
        when(cardAuthorizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardAuthorization result = service.execute(authorization, UUID.randomUUID(), UUID.randomUUID());

        assertThat(result.getStatus()).isEqualTo(CardAuthorizationStatus.DECLINED);
        assertThat(result.getDeclineReason()).contains("insufficient funds");
    }

    @Test
    void aLedgerErrorResponseBodyIsUnwrappedIntoACleanDeclineReason() {
        // ledger-service's GlobalExceptionHandler returns bodies shaped like
        // {"status":422,"error":"...","timestamp":"..."} — this test proves
        // we surface that "error" text instead of the raw HTTP exception dump.
        CardAuthorizationExecutionService service = new CardAuthorizationExecutionService(cardAuthorizationRepository, ledgerClient, objectMapper);
        CardAuthorization authorization = CardAuthorization.pendingLedgerDecision(
                UUID.randomUUID(), UUID.randomUUID(), "Grocery Store", new BigDecimal("120.00"), "USD");

        String responseBody = "{\"status\":422,\"error\":\"Ledger account has insufficient funds\",\"timestamp\":\"2026-01-01T00:00:00Z\"}";
        HttpClientErrorException upstreamError = new HttpClientErrorException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                responseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.UTF_8);
        when(ledgerClient.postCardPurchase(any(), any(), any(), any(), any())).thenThrow(upstreamError);
        when(cardAuthorizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardAuthorization result = service.execute(authorization, UUID.randomUUID(), UUID.randomUUID());

        assertThat(result.getStatus()).isEqualTo(CardAuthorizationStatus.DECLINED);
        assertThat(result.getDeclineReason()).isEqualTo("Ledger account has insufficient funds");
    }
}
