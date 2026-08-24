package com.fintechplatform.transfers.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.transfers.client.AccountResponse;
import com.fintechplatform.transfers.client.JournalEntryResponse;
import com.fintechplatform.transfers.client.LedgerClient;
import com.fintechplatform.transfers.domain.Transfer;
import com.fintechplatform.transfers.domain.TransferStatus;
import com.fintechplatform.transfers.repository.TransferRepository;
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
class TransferExecutionServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private LedgerClient ledgerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void aSuccessfulLedgerPostingCompletesTheTransfer() {
        TransferExecutionService service = new TransferExecutionService(transferRepository, ledgerClient, objectMapper);
        Transfer transfer = new Transfer(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("40.00"), "USD");
        UUID journalEntryId = UUID.randomUUID();

        when(ledgerClient.postTransfer(any(), any(), any(), any(), any()))
                .thenReturn(new JournalEntryResponse(journalEntryId, "Transfer", transfer.getId().toString(), Instant.now()));
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer result = service.execute(transfer, account(), account(), "Transfer");

        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(result.getJournalEntryReference()).isEqualTo(journalEntryId);
    }

    @Test
    void aLedgerFailureMarksTheTransferFailedInsteadOfThrowing() {
        TransferExecutionService service = new TransferExecutionService(transferRepository, ledgerClient, objectMapper);
        Transfer transfer = new Transfer(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("999999.00"), "USD");

        when(ledgerClient.postTransfer(any(), any(), any(), any(), any())).thenThrow(new RestClientException("insufficient funds"));
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer result = service.execute(transfer, account(), account(), "Transfer");

        assertThat(result.getStatus()).isEqualTo(TransferStatus.FAILED);
        assertThat(result.getFailureReason()).contains("insufficient funds");
    }

    @Test
    void aLedgerErrorResponseBodyIsUnwrappedIntoACleanFailureReason() {
        // ledger-service's GlobalExceptionHandler returns bodies shaped like
        // {"status":422,"error":"...","timestamp":"..."} — this test proves
        // we surface that "error" text instead of the raw HTTP exception dump.
        TransferExecutionService service = new TransferExecutionService(transferRepository, ledgerClient, objectMapper);
        Transfer transfer = new Transfer(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("500.00"), "USD");

        String responseBody = "{\"status\":422,\"error\":\"Ledger account has insufficient funds\",\"timestamp\":\"2026-01-01T00:00:00Z\"}";
        HttpClientErrorException upstreamError = new HttpClientErrorException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                responseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.UTF_8);
        when(ledgerClient.postTransfer(any(), any(), any(), any(), any())).thenThrow(upstreamError);
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer result = service.execute(transfer, account(), account(), "Transfer");

        assertThat(result.getStatus()).isEqualTo(TransferStatus.FAILED);
        assertThat(result.getFailureReason()).isEqualTo("Ledger account has insufficient funds");
    }

    private AccountResponse account() {
        return new AccountResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "ACC-1", "CHECKING", "USD", "ACTIVE", Instant.now());
    }
}
