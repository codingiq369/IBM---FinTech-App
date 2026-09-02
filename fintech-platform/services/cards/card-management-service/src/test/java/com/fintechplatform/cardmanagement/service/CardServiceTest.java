package com.fintechplatform.cardmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.cardmanagement.client.AccountResponse;
import com.fintechplatform.cardmanagement.client.AccountsClient;
import com.fintechplatform.cardmanagement.domain.Card;
import com.fintechplatform.cardmanagement.domain.CardStatus;
import com.fintechplatform.cardmanagement.dto.IssueCardRequest;
import com.fintechplatform.cardmanagement.repository.CardRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountsClient accountsClient;

    private final CardNumberGenerator cardNumberGenerator = new CardNumberGenerator();

    @Test
    void issuingACardAgainstAnActiveAccountSucceeds() {
        CardService service = new CardService(cardRepository, accountsClient, cardNumberGenerator);
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(accountsClient.getAccount(accountId))
                .thenReturn(new AccountResponse(accountId, customerId, UUID.randomUUID(), "ACC-1", "CHECKING", "USD", "ACTIVE", Instant.now()));
        when(cardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Card card = service.issueCard(new IssueCardRequest(accountId, "Jane Doe", null));

        assertThat(card.getAccountId()).isEqualTo(accountId);
        assertThat(card.getCustomerId()).isEqualTo(customerId);
        assertThat(card.getStatus()).isEqualTo(CardStatus.ISSUED);
        assertThat(card.getDailyPurchaseLimit()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void issuingACardAgainstAClosedAccountIsRejectedWithoutEverSavingIt() {
        CardService service = new CardService(cardRepository, accountsClient, cardNumberGenerator);
        UUID accountId = UUID.randomUUID();

        when(accountsClient.getAccount(accountId))
                .thenReturn(new AccountResponse(accountId, UUID.randomUUID(), UUID.randomUUID(), "ACC-2", "CHECKING", "USD", "CLOSED", Instant.now()));

        assertThatThrownBy(() -> service.issueCard(new IssueCardRequest(accountId, "Jane Doe", null)))
                .isInstanceOf(AccountNotEligibleException.class);

        verify(cardRepository, never()).save(any());
    }

    @Test
    void activatingAnIssuedCardMovesItToActive() {
        CardService service = new CardService(cardRepository, accountsClient, cardNumberGenerator);
        Card card = new Card(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "•••• •••• •••• 1234", "1234", "Jane Doe",
                com.fintechplatform.cardmanagement.domain.CardType.DEBIT, new BigDecimal("2000.00"));
        when(cardRepository.findById(card.getId())).thenReturn(java.util.Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Card activated = service.activateCard(card.getId());

        assertThat(activated.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(activated.getActivatedAt()).isNotNull();
    }

    @Test
    void activatingAnAlreadyActiveCardIsRejected() {
        CardService service = new CardService(cardRepository, accountsClient, cardNumberGenerator);
        Card card = new Card(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "•••• •••• •••• 1234", "1234", "Jane Doe",
                com.fintechplatform.cardmanagement.domain.CardType.DEBIT, new BigDecimal("2000.00"));
        card.activate();
        when(cardRepository.findById(card.getId())).thenReturn(java.util.Optional.of(card));

        assertThatThrownBy(() -> service.activateCard(card.getId())).isInstanceOf(IllegalStateException.class);
    }
}
