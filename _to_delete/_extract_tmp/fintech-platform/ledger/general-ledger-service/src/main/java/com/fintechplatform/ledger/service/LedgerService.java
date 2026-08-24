package com.fintechplatform.ledger.service;

import com.fintechplatform.ledger.domain.Direction;
import com.fintechplatform.ledger.domain.JournalEntry;
import com.fintechplatform.ledger.domain.LedgerAccount;
import com.fintechplatform.ledger.domain.Posting;
import com.fintechplatform.ledger.dto.OpenLedgerAccountRequest;
import com.fintechplatform.ledger.dto.PostJournalEntryRequest;
import com.fintechplatform.ledger.dto.PostingRequest;
import com.fintechplatform.ledger.repository.JournalEntryRepository;
import com.fintechplatform.ledger.repository.LedgerAccountRepository;
import com.fintechplatform.ledger.repository.PostingRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final PostingRepository postingRepository;

    public LedgerService(
            LedgerAccountRepository ledgerAccountRepository,
            JournalEntryRepository journalEntryRepository,
            PostingRepository postingRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.postingRepository = postingRepository;
    }

    @Transactional
    public LedgerAccount openAccount(OpenLedgerAccountRequest request) {
        LedgerAccount account = new LedgerAccount(request.ownerReference(), request.currencyOrDefault());
        return ledgerAccountRepository.save(account);
    }

    public LedgerAccount getAccount(UUID ledgerAccountId) {
        return ledgerAccountRepository.findById(ledgerAccountId).orElseThrow(() -> new LedgerAccountNotFoundException(ledgerAccountId));
    }

    /**
     * A ledger account is credit-normal (see {@link Direction}), so its
     * balance is simply everything ever credited to it minus everything
     * ever debited from it. Nothing is cached — this always recomputes from
     * the full posting history, which is the whole point of a ledger: the
     * balance is a derived fact, not a stored one.
     */
    public BigDecimal getBalance(UUID ledgerAccountId) {
        getAccount(ledgerAccountId); // throws if the account doesn't exist
        BigDecimal totalCredits = postingRepository.sumAmountByLedgerAccountIdAndDirection(ledgerAccountId, Direction.CREDIT);
        BigDecimal totalDebits = postingRepository.sumAmountByLedgerAccountIdAndDirection(ledgerAccountId, Direction.DEBIT);
        return totalCredits.subtract(totalDebits);
    }

    public List<Posting> getPostingHistory(UUID ledgerAccountId) {
        getAccount(ledgerAccountId);
        return postingRepository.findByLedgerAccountIdOrderByCreatedAtDesc(ledgerAccountId);
    }

    /**
     * Posts an atomic movement of money. Two independent safeguards run
     * before anything is written:
     * 1. Every referenced ledger account must actually exist.
     * 2. No account may be driven to a negative balance by this entry.
     * A third safeguard — debits must equal credits — is enforced by
     * {@link JournalEntry}'s own constructor, so it can never be skipped
     * even by future code that calls it directly.
     */
    @Transactional
    public JournalEntry postJournalEntry(PostJournalEntryRequest request) {
        List<PostingRequest> postingRequests = request.postings();

        Set<UUID> accountIds = postingRequests.stream().map(PostingRequest::ledgerAccountId).collect(Collectors.toSet());
        for (UUID accountId : accountIds) {
            if (!ledgerAccountRepository.existsById(accountId)) {
                throw new LedgerAccountNotFoundException(accountId);
            }
        }

        for (UUID accountId : accountIds) {
            BigDecimal netEffect = netEffectFor(accountId, postingRequests);
            if (netEffect.signum() < 0) {
                BigDecimal currentBalance = getBalance(accountId);
                BigDecimal projectedBalance = currentBalance.add(netEffect);
                if (projectedBalance.signum() < 0) {
                    throw new InsufficientFundsException(accountId, currentBalance, netEffect.abs());
                }
            }
        }

        List<Posting> postings = postingRequests.stream()
                .map(p -> new Posting(p.ledgerAccountId(), p.direction(), p.amount()))
                .toList();

        JournalEntry entry = new JournalEntry(request.description(), request.transactionReference(), postings);
        return journalEntryRepository.save(entry);
    }

    /** Net change to an account's balance from just this entry's postings:
     * credits minus debits for that account within the request. Computed
     * per-account (not per-posting) so an entry that both credits and
     * debits the same account still nets out correctly. */
    private BigDecimal netEffectFor(UUID accountId, List<PostingRequest> postings) {
        BigDecimal credits = sumMatching(postings, accountId, Direction.CREDIT);
        BigDecimal debits = sumMatching(postings, accountId, Direction.DEBIT);
        return credits.subtract(debits);
    }

    private BigDecimal sumMatching(List<PostingRequest> postings, UUID accountId, Direction direction) {
        return postings.stream()
                .filter(p -> p.ledgerAccountId().equals(accountId) && p.direction() == direction)
                .map(PostingRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
