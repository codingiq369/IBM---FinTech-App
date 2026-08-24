package com.fintechplatform.accounts.dto;

import com.fintechplatform.accounts.domain.Account;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        UUID ledgerAccountId,
        String accountNumber,
        String accountType,
        String currency,
        String status,
        Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getLedgerAccountId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt());
    }
}
