package com.fintechplatform.transfers.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.transfers.client.AccountResponse;
import com.fintechplatform.transfers.client.AccountsClient;
import com.fintechplatform.transfers.domain.Transfer;
import com.fintechplatform.transfers.dto.InitiateTransferRequest;
import com.fintechplatform.transfers.repository.TransferRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private TransferExecutionService transferExecutionService;

    @Test
    void transferringToYourselfIsRejectedBeforeAnyAccountLookup() {
        TransferService service = new TransferService(transferRepository, accountsClient, transferExecutionService);
        UUID accountId = UUID.randomUUID();

        assertThatThrownBy(() -> service.initiateTransfer(new InitiateTransferRequest(accountId, accountId, new BigDecimal("10.00"), null)))
                .isInstanceOf(InvalidTransferException.class);

        verify(accountsClient, never()).getAccount(any());
    }

    @Test
    void mismatchedCurrenciesAreRejectedBeforeAnyTransferIsSaved() {
        TransferService service = new TransferService(transferRepository, accountsClient, transferExecutionService);
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        when(accountsClient.getAccount(sourceId)).thenReturn(account(sourceId, "USD", true));
        when(accountsClient.getAccount(destinationId)).thenReturn(account(destinationId, "EUR", true));

        assertThatThrownBy(() -> service.initiateTransfer(new InitiateTransferRequest(sourceId, destinationId, new BigDecimal("10.00"), null)))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("Currency mismatch");

        verify(transferRepository, never()).save(any());
    }

    @Test
    void aClosedDestinationAccountIsRejected() {
        TransferService service = new TransferService(transferRepository, accountsClient, transferExecutionService);
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        when(accountsClient.getAccount(sourceId)).thenReturn(account(sourceId, "USD", true));
        when(accountsClient.getAccount(destinationId)).thenReturn(account(destinationId, "USD", false));

        assertThatThrownBy(() -> service.initiateTransfer(new InitiateTransferRequest(sourceId, destinationId, new BigDecimal("10.00"), null)))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void aValidTransferIsSavedAsPendingThenHandedToExecution() {
        TransferService service = new TransferService(transferRepository, accountsClient, transferExecutionService);
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        AccountResponse source = account(sourceId, "USD", true);
        AccountResponse destination = account(destinationId, "USD", true);

        when(accountsClient.getAccount(sourceId)).thenReturn(source);
        when(accountsClient.getAccount(destinationId)).thenReturn(destination);
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferExecutionService.execute(any(), any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer result = service.initiateTransfer(new InitiateTransferRequest(sourceId, destinationId, new BigDecimal("25.00"), "Rent"));

        assertThat(result.getSourceAccountId()).isEqualTo(sourceId);
        assertThat(result.getDestinationAccountId()).isEqualTo(destinationId);
        assertThat(result.getAmount()).isEqualByComparingTo("25.00");
        verify(transferExecutionService).execute(any(), any(), any(), any());
    }

    private AccountResponse account(UUID id, String currency, boolean active) {
        return new AccountResponse(
                id, UUID.randomUUID(), UUID.randomUUID(), "ACC-1", "CHECKING", currency, active ? "ACTIVE" : "CLOSED", Instant.now());
    }
}
