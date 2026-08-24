package com.fintechplatform.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fintechplatform.ledger.domain.Direction;
import com.fintechplatform.ledger.domain.JournalEntry;
import com.fintechplatform.ledger.dto.PostJournalEntryRequest;
import com.fintechplatform.ledger.dto.PostingRequest;
import com.fintechplatform.ledger.repository.JournalEntryRepository;
import com.fintechplatform.ledger.repository.LedgerAccountRepository;
import com.fintechplatform.ledger.repository.PostingRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private PostingRepository postingRepository;

    @Test
    void postingAJournalEntryFailsFastIfAnAccountDoesNotExist() {
        LedgerService service = new LedgerService(ledgerAccountRepository, journalEntryRepository, postingRepository);
        UUID missingAccount = UUID.randomUUID();
        UUID otherAccount = UUID.randomUUID();
        when(ledgerAccountRepository.existsById(any())).thenReturn(false);

        PostJournalEntryRequest request = new PostJournalEntryRequest(
                "Transfer",
                "txn-1",
                List.of(
                        new PostingRequest(missingAccount, Direction.DEBIT, new BigDecimal("10.00")),
                        new PostingRequest(otherAccount, Direction.CREDIT, new BigDecimal("10.00"))));

        assertThatThrownBy(() -> service.postJournalEntry(request)).isInstanceOf(LedgerAccountNotFoundException.class);
    }

    @Test
    void postingAJournalEntryFailsIfTheDebitedAccountCannotCoverIt() {
        LedgerService service = new LedgerService(ledgerAccountRepository, journalEntryRepository, postingRepository);
        UUID source = UUID.randomUUID();
        UUID destination = UUID.randomUUID();

        when(ledgerAccountRepository.existsById(any())).thenReturn(true);
        // Source account has a balance of 5.00 (say, 5 credited, 0 debited so far).
        when(postingRepository.sumAmountByLedgerAccountIdAndDirection(source, Direction.CREDIT)).thenReturn(new BigDecimal("5.00"));
        when(postingRepository.sumAmountByLedgerAccountIdAndDirection(source, Direction.DEBIT)).thenReturn(BigDecimal.ZERO);

        PostJournalEntryRequest request = new PostJournalEntryRequest(
                "Transfer more than the balance",
                "txn-2",
                List.of(
                        new PostingRequest(source, Direction.DEBIT, new BigDecimal("10.00")),
                        new PostingRequest(destination, Direction.CREDIT, new BigDecimal("10.00"))));

        assertThatThrownBy(() -> service.postJournalEntry(request)).isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void postingAValidJournalEntrySucceeds() {
        LedgerService service = new LedgerService(ledgerAccountRepository, journalEntryRepository, postingRepository);
        UUID source = UUID.randomUUID();
        UUID destination = UUID.randomUUID();

        when(ledgerAccountRepository.existsById(any())).thenReturn(true);
        when(postingRepository.sumAmountByLedgerAccountIdAndDirection(source, Direction.CREDIT)).thenReturn(new BigDecimal("100.00"));
        when(postingRepository.sumAmountByLedgerAccountIdAndDirection(source, Direction.DEBIT)).thenReturn(BigDecimal.ZERO);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PostJournalEntryRequest request = new PostJournalEntryRequest(
                "Transfer within balance",
                "txn-3",
                List.of(
                        new PostingRequest(source, Direction.DEBIT, new BigDecimal("30.00")),
                        new PostingRequest(destination, Direction.CREDIT, new BigDecimal("30.00"))));

        JournalEntry entry = service.postJournalEntry(request);

        assertThat(entry.getPostings()).hasSize(2);
    }

    @Test
    void balanceIsCreditsMinusDebits() {
        LedgerService service = new LedgerService(ledgerAccountRepository, journalEntryRepository, postingRepository);
        UUID accountId = UUID.randomUUID();
        when(ledgerAccountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(new com.fintechplatform.ledger.domain.LedgerAccount(UUID.randomUUID(), "USD")));
        when(postingRepository.sumAmountByLedgerAccountIdAndDirection(accountId, Direction.CREDIT)).thenReturn(new BigDecimal("120.00"));
        when(postingRepository.sumAmountByLedgerAccountIdAndDirection(accountId, Direction.DEBIT)).thenReturn(new BigDecimal("45.00"));

        BigDecimal balance = service.getBalance(accountId);

        assertThat(balance).isEqualByComparingTo("75.00");
    }
}
