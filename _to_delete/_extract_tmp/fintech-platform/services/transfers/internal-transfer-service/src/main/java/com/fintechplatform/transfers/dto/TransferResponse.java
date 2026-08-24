package com.fintechplatform.transfers.dto;

import com.fintechplatform.transfers.domain.Transfer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String status,
        UUID journalEntryReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus().name(),
                transfer.getJournalEntryReference(),
                transfer.getFailureReason(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt());
    }
}
