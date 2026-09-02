package com.fintechplatform.cardauthorization.service;

import com.fintechplatform.cardauthorization.client.LedgerClient;
import com.fintechplatform.cardauthorization.domain.ClearingAccount;
import com.fintechplatform.cardauthorization.repository.ClearingAccountRepository;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Finds — or, the first time a currency is used, creates — the card
 * network's clearing ledger account for that currency (see ADR-0010). This
 * is a get-or-create against two systems (our own {@code clearing_accounts}
 * table and ledger-service), so a race between two concurrent first-ever
 * purchases in the same new currency is handled explicitly: whichever
 * request loses the unique-constraint race on {@code currency} simply reads
 * back what the winner just created, rather than erroring out or leaving a
 * second, orphaned ledger account behind.
 *
 * <p>No {@code @Transactional} boundary of its own is needed here — every
 * {@link ClearingAccountRepository} call is already transactional on its
 * own (Spring Data wraps each CRUD method individually), and a
 * class-internal {@code @Transactional} on a method called via {@code this}
 * would silently do nothing anyway, since Spring's proxy is never in the
 * call path for a self-invocation (see the class comment on
 * {@code TransferExecutionService} in transfers-service for the long-form
 * explanation of that pitfall — this class sidesteps it by not needing a
 * multi-statement transaction at all).
 */
@Service
public class ClearingAccountService {

    private final ClearingAccountRepository clearingAccountRepository;
    private final LedgerClient ledgerClient;

    public ClearingAccountService(ClearingAccountRepository clearingAccountRepository, LedgerClient ledgerClient) {
        this.clearingAccountRepository = clearingAccountRepository;
        this.ledgerClient = ledgerClient;
    }

    public UUID getOrCreateClearingLedgerAccountId(String currency) {
        return clearingAccountRepository.findById(currency)
                .map(ClearingAccount::getLedgerAccountId)
                .orElseGet(() -> createClearingAccount(currency));
    }

    private UUID createClearingAccount(String currency) {
        try {
            UUID ledgerAccountId = ledgerClient.openClearingLedgerAccount(currency).id();
            clearingAccountRepository.save(new ClearingAccount(currency, ledgerAccountId));
            return ledgerAccountId;
        } catch (DataIntegrityViolationException raceLost) {
            return clearingAccountRepository.findById(currency)
                    .map(ClearingAccount::getLedgerAccountId)
                    .orElseThrow(() -> raceLost);
        }
    }
}
