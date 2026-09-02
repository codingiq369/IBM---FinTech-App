package com.fintechplatform.cardauthorization.service;

import com.fintechplatform.cardauthorization.client.AccountResponse;
import com.fintechplatform.cardauthorization.client.AccountsClient;
import com.fintechplatform.cardauthorization.client.CardManagementClient;
import com.fintechplatform.cardauthorization.client.CardResponse;
import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import com.fintechplatform.cardauthorization.domain.CardAuthorizationStatus;
import com.fintechplatform.cardauthorization.dto.AuthorizePurchaseRequest;
import com.fintechplatform.cardauthorization.event.CardAuthorizationEventPublisher;
import com.fintechplatform.cardauthorization.repository.CardAuthorizationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CardAuthorizationService {

    private final CardAuthorizationRepository cardAuthorizationRepository;
    private final CardManagementClient cardManagementClient;
    private final AccountsClient accountsClient;
    private final ClearingAccountService clearingAccountService;
    private final CardAuthorizationExecutionService executionService;
    private final CardAuthorizationEventPublisher eventPublisher;

    public CardAuthorizationService(
            CardAuthorizationRepository cardAuthorizationRepository,
            CardManagementClient cardManagementClient,
            AccountsClient accountsClient,
            ClearingAccountService clearingAccountService,
            CardAuthorizationExecutionService executionService,
            CardAuthorizationEventPublisher eventPublisher) {
        this.cardAuthorizationRepository = cardAuthorizationRepository;
        this.cardManagementClient = cardManagementClient;
        this.accountsClient = accountsClient;
        this.clearingAccountService = clearingAccountService;
        this.executionService = executionService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * The real-time authorization decision, in order:
     * <ol>
     *   <li>The card must exist — if not, the request itself was invalid
     *       (404), nothing is recorded.</li>
     *   <li>The card must be ACTIVE — an ISSUED-but-not-activated, BLOCKED,
     *       or CLOSED card produces a recorded DECLINED authorization, not
     *       an error, exactly like a real card network would respond.</li>
     *   <li>The purchase must fit under the card's daily purchase limit,
     *       checked against the sum of everything already APPROVED for
     *       this card since midnight UTC — another recorded DECLINED, not
     *       an error, if it doesn't.</li>
     *   <li>Only once both local checks pass does this call out to
     *       ledger-service at all, via {@link CardAuthorizationExecutionService},
     *       which makes the final APPROVED/DECLINED call (e.g. insufficient
     *       funds) and posts the balanced journal entry when it approves.
     *       This ordering matters: a card that's blocked or over its limit
     *       never even reaches the ledger. Only an APPROVED outcome is
     *       published as a {@code CardAuthorizationApproved} event via
     *       {@link CardAuthorizationEventPublisher} — a DECLINED purchase
     *       is a successfully recorded API response, not something
     *       downstream consumers need to react to.</li>
     * </ol>
     * This is a simplified stand-in for a real issuer's authorization
     * engine (which would also run fraud scoring, velocity checks, and a
     * hold/settlement split) — enough to show the shape without building
     * all of that.
     */
    public CardAuthorization authorizePurchase(AuthorizePurchaseRequest request) {
        CardResponse card = cardManagementClient.getCard(request.cardId());
        String currency = request.currencyOrDefault();

        if (!card.isActive()) {
            return cardAuthorizationRepository.save(CardAuthorization.declined(
                    card.id(), card.accountId(), request.merchantName(), request.amount(), currency,
                    "Card is not active (status: " + card.status() + ")"));
        }

        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);
        BigDecimal alreadyApprovedToday =
                cardAuthorizationRepository.sumApprovedAmountSince(card.id(), CardAuthorizationStatus.APPROVED, startOfToday);
        BigDecimal projectedTotal = alreadyApprovedToday.add(request.amount());
        if (projectedTotal.compareTo(card.dailyPurchaseLimit()) > 0) {
            return cardAuthorizationRepository.save(CardAuthorization.declined(
                    card.id(), card.accountId(), request.merchantName(), request.amount(), currency,
                    "Daily purchase limit of " + card.dailyPurchaseLimit() + " " + currency + " would be exceeded"));
        }

        AccountResponse account = accountsClient.getAccount(card.accountId());
        UUID clearingLedgerAccountId = clearingAccountService.getOrCreateClearingLedgerAccountId(currency);

        // Not persisted yet on purpose: CardAuthorization has no PENDING
        // status (unlike Transfer), so there is nothing valid to write
        // until executionService.execute() resolves it to APPROVED or
        // DECLINED and performs the one and only insert, in its own
        // transaction, a few lines from here.
        CardAuthorization authorization =
                CardAuthorization.pendingLedgerDecision(card.id(), card.accountId(), request.merchantName(), request.amount(), currency);

        CardAuthorization result = executionService.execute(authorization, account.ledgerAccountId(), clearingLedgerAccountId);
        if (result.getStatus() == CardAuthorizationStatus.APPROVED) {
            eventPublisher.publishCardAuthorizationApproved(result);
        }
        return result;
    }

    public CardAuthorization getById(UUID id) {
        return cardAuthorizationRepository.findById(id).orElseThrow(() -> new CardAuthorizationNotFoundException(id));
    }

    public List<CardAuthorization> getByCardId(UUID cardId) {
        return cardAuthorizationRepository.findByCardIdOrderByCreatedAtDesc(cardId);
    }
}
