package com.fintechplatform.accounts.service;

import com.fintechplatform.accounts.client.CustomerClient;
import com.fintechplatform.accounts.client.LedgerAccountResponse;
import com.fintechplatform.accounts.client.LedgerBalanceResponse;
import com.fintechplatform.accounts.client.LedgerClient;
import com.fintechplatform.accounts.domain.Account;
import com.fintechplatform.accounts.dto.AccountBalanceResponse;
import com.fintechplatform.accounts.dto.OpenAccountRequest;
import com.fintechplatform.accounts.repository.AccountRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient;
    private final LedgerClient ledgerClient;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountService(
            AccountRepository accountRepository,
            CustomerClient customerClient,
            LedgerClient ledgerClient,
            AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.customerClient = customerClient;
        this.ledgerClient = ledgerClient;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    /**
     * Opening an account touches two other services in sequence:
     * 1. customer-service confirms this is a real, KYC-approved customer.
     * 2. ledger-service creates the account's entry in the general ledger
     *    (starting balance zero, no postings yet).
     * Only once both succeed do we persist our own Account record. We mint
     * the account id ourselves (rather than letting the database generate
     * it) so we have a stable id to hand to ledger-service as the "owner
     * reference" before this row exists at all — that keeps this method to
     * a single insert instead of insert-then-patch.
     */
    @Transactional
    public Account openAccount(OpenAccountRequest request) {
        if (!customerClient.isApprovedCustomer(request.customerId())) {
            throw new CustomerNotApprovedException(request.customerId());
        }

        UUID accountId = UUID.randomUUID();
        String currency = request.currencyOrDefault();
        String accountNumber = accountNumberGenerator.generate();

        LedgerAccountResponse ledgerAccount = ledgerClient.openLedgerAccount(accountId, currency);

        Account account = new Account(accountId, request.customerId(), ledgerAccount.id(), accountNumber, request.accountType(), currency);
        return accountRepository.save(account);
    }

    public Account getById(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public List<Account> getByCustomerId(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public AccountBalanceResponse getBalance(UUID accountId) {
        Account account = getById(accountId);
        LedgerBalanceResponse ledgerBalance = ledgerClient.getBalance(account.getLedgerAccountId());
        return new AccountBalanceResponse(account.getId(), account.getAccountNumber(), ledgerBalance.balance(), account.getCurrency());
    }
}
