package com.fintechplatform.cardauthorization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.cardauthorization.client.LedgerAccountResponse;
import com.fintechplatform.cardauthorization.client.LedgerClient;
import com.fintechplatform.cardauthorization.domain.ClearingAccount;
import com.fintechplatform.cardauthorization.repository.ClearingAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClearingAccountServiceTest {

    @Mock
    private ClearingAccountRepository clearingAccountRepository;

    @Mock
    private LedgerClient ledgerClient;

    @Test
    void anExistingClearingAccountIsReusedWithoutCallingTheLedger() {
        ClearingAccountService service = new ClearingAccountService(clearingAccountRepository, ledgerClient);
        UUID existingLedgerAccountId = UUID.randomUUID();
        when(clearingAccountRepository.findById("USD"))
                .thenReturn(Optional.of(new ClearingAccount("USD", existingLedgerAccountId)));

        UUID result = service.getOrCreateClearingLedgerAccountId("USD");

        assertThat(result).isEqualTo(existingLedgerAccountId);
        verify(ledgerClient, never()).openClearingLedgerAccount(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void aNewCurrencyOpensAndPersistsAClearingAccount() {
        ClearingAccountService service = new ClearingAccountService(clearingAccountRepository, ledgerClient);
        UUID newLedgerAccountId = UUID.randomUUID();
        when(clearingAccountRepository.findById("EUR")).thenReturn(Optional.empty());
        when(ledgerClient.openClearingLedgerAccount("EUR"))
                .thenReturn(new LedgerAccountResponse(newLedgerAccountId, "CARD_NETWORK_CLEARING:EUR", "EUR", Instant.now()));

        UUID result = service.getOrCreateClearingLedgerAccountId("EUR");

        assertThat(result).isEqualTo(newLedgerAccountId);
        verify(clearingAccountRepository).save(org.mockito.ArgumentMatchers.argThat(
                account -> account.getCurrency().equals("EUR") && account.getLedgerAccountId().equals(newLedgerAccountId)));
    }
}
