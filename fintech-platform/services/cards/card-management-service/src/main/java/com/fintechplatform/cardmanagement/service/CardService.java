package com.fintechplatform.cardmanagement.service;

import com.fintechplatform.cardmanagement.client.AccountResponse;
import com.fintechplatform.cardmanagement.client.AccountsClient;
import com.fintechplatform.cardmanagement.domain.Card;
import com.fintechplatform.cardmanagement.domain.CardType;
import com.fintechplatform.cardmanagement.dto.IssueCardRequest;
import com.fintechplatform.cardmanagement.repository.CardRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AccountsClient accountsClient;
    private final CardNumberGenerator cardNumberGenerator;

    public CardService(CardRepository cardRepository, AccountsClient accountsClient, CardNumberGenerator cardNumberGenerator) {
        this.cardRepository = cardRepository;
        this.accountsClient = accountsClient;
        this.cardNumberGenerator = cardNumberGenerator;
    }

    /**
     * Issuing a card checks with accounts-service that the account is real
     * and ACTIVE, then mints a card in ISSUED status. It does not touch
     * ledger-service at all — a card has nothing of its own to open in the
     * ledger, it only ever authorizes movements against its linked
     * account's existing ledger account (see card-authorization-service).
     */
    @Transactional
    public Card issueCard(IssueCardRequest request) {
        AccountResponse account = accountsClient.getAccount(request.accountId());
        if (!account.isActive()) {
            throw new AccountNotEligibleException(account.id(), account.status());
        }

        CardNumberGenerator.Generated cardNumber = cardNumberGenerator.generate();
        Card card = new Card(
                UUID.randomUUID(),
                account.id(),
                account.customerId(),
                cardNumber.masked(),
                cardNumber.lastFour(),
                request.cardholderName(),
                CardType.DEBIT,
                request.dailyPurchaseLimitOrDefault());

        return cardRepository.save(card);
    }

    @Transactional
    public Card activateCard(UUID cardId) {
        Card card = getById(cardId);
        card.activate();
        return cardRepository.save(card);
    }

    @Transactional
    public Card blockCard(UUID cardId) {
        Card card = getById(cardId);
        card.block();
        return cardRepository.save(card);
    }

    public Card getById(UUID id) {
        return cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
    }

    public List<Card> getByAccountId(UUID accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    public List<Card> getByCustomerId(UUID customerId) {
        return cardRepository.findByCustomerId(customerId);
    }
}
