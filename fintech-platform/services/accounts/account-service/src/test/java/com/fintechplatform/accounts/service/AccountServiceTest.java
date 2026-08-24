package com.fintechplatform.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.accounts.client.CustomerClient;
import com.fintechplatform.accounts.client.LedgerAccountResponse;
import com.fintechplatform.accounts.client.LedgerClient;
import com.fintechplatform.accounts.domain.Account;
import com.fintechplatform.accounts.domain.AccountType;
import com.fintechplatform.accounts.dto.OpenAccountRequest;
import com.fintechplatform.accounts.repository.AccountRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private LedgerClient ledgerClient;

    private final AccountNumberGenerator accountNumberGenerator = new AccountNumberGenerator();

    @Test
    void openingAnAccountForAnApprovedCustomerRegistersItWithTheLedgerFirst() {
        AccountService service = new AccountService(accountRepository, customerClient, ledgerClient, accountNumberGenerator);
        UUID customerId = UUID.randomUUID();
        UUID ledgerAccountId = UUID.randomUUID();

        when(customerClient.isApprovedCustomer(customerId)).thenReturn(true);
        when(ledgerClient.openLedgerAccount(any(), org.mockito.ArgumentMatchers.eq("USD")))
                .thenReturn(new LedgerAccountResponse(ledgerAccountId, null, "USD", Instant.now()));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = service.openAccount(new OpenAccountRequest(customerId, AccountType.CHECKING, "USD"));

        assertThat(account.getLedgerAccountId()).isEqualTo(ledgerAccountId);
        assertThat(account.getCustomerId()).isEqualTo(customerId);
        assertThat(account.isActive()).isTrue();
    }

    @Test
    void openingAnAccountForAnUnapprovedCustomerNeverCallsTheLedger() {
        AccountService service = new AccountService(accountRepository, customerClient, ledgerClient, accountNumberGenerator);
        UUID customerId = UUID.randomUUID();
        when(customerClient.isApprovedCustomer(customerId)).thenReturn(false);

        assertThatThrownBy(() -> service.openAccount(new OpenAccountRequest(customerId, AccountType.SAVINGS, "USD")))
                .isInstanceOf(CustomerNotApprovedException.class);

        verify(ledgerClient, never()).openLedgerAccount(any(), any());
        verify(accountRepository, never()).save(any());
    }
}
