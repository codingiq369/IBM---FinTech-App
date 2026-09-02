package com.fintechplatform.cardauthorization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.cardauthorization.client.AccountsClient;
import com.fintechplatform.cardauthorization.client.CardManagementClient;
import com.fintechplatform.cardauthorization.client.CardResponse;
import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import com.fintechplatform.cardauthorization.domain.CardAuthorizationStatus;
import com.fintechplatform.cardauthorization.dto.AuthorizePurchaseRequest;
import com.fintechplatform.cardauthorization.repository.CardAuthorizationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardAuthorizationServiceTest {

    @Mock
    private CardAuthorizationRepository cardAuthorizationRepository;

    @Mock
    private CardManagementClient cardManagementClient;

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private ClearingAccountService clearingAccountService;

    @Mock
    private CardAuthorizationExecutionService executionService;

    @Test
    void aPurchaseOnANonActiveCardIsDeclinedWithoutEverCallingTheLedger() {
        CardAuthorizationService service = new CardAuthorizationService(
                cardAuthorizationRepository, cardManagementClient, accountsClient, clearingAccountService, executionService);
        UUID cardId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(cardManagementClient.getCard(cardId)).thenReturn(
                new CardResponse(cardId, accountId, UUID.randomUUID(), "•••• 1234", "Jane Doe", "DEBIT", 1, 2030,
                        "BLOCKED", new BigDecimal("2000.00"), Instant.now(), null, Instant.now()));
        when(cardAuthorizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardAuthorization result = service.authorizePurchase(new AuthorizePurchaseRequest(cardId, "Coffee Shop", new BigDecimal("4.50"), "USD"));

        assertThat(result.getStatus()).isEqualTo(CardAuthorizationStatus.DECLINED);
        assertThat(result.getDeclineReason()).contains("not active");
        verify(executionService, never()).execute(any(), any(), any());
        verify(accountsClient, never()).getAccount(any());
    }

    @Test
    void aPurchaseThatWouldExceedTheDailyLimitIsDeclinedWithoutEverCallingTheLedger() {
        CardAuthorizationService service = new CardAuthorizationService(
                cardAuthorizationRepository, cardManagementClient, accountsClient, clearingAccountService, executionService);
        UUID cardId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(cardManagementClient.getCard(cardId)).thenReturn(
                new CardResponse(cardId, accountId, UUID.randomUUID(), "•••• 1234", "Jane Doe", "DEBIT", 1, 2030,
                        "ACTIVE", new BigDecimal("100.00"), Instant.now(), Instant.now(), null));
        when(cardAuthorizationRepository.sumApprovedAmountSince(any(), any(), any())).thenReturn(new BigDecimal("80.00"));
        when(cardAuthorizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardAuthorization result = service.authorizePurchase(new AuthorizePurchaseRequest(cardId, "Electronics Store", new BigDecimal("50.00"), "USD"));

        assertThat(result.getStatus()).isEqualTo(CardAuthorizationStatus.DECLINED);
        assertThat(result.getDeclineReason()).contains("Daily purchase limit");
        verify(executionService, never()).execute(any(), any(), any());
    }

    @Test
    void aPurchaseWithinTheLimitOnAnActiveCardIsHandedToTheExecutionService() {
        CardAuthorizationService service = new CardAuthorizationService(
                cardAuthorizationRepository, cardManagementClient, accountsClient, clearingAccountService, executionService);
        UUID cardId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID ledgerAccountId = UUID.randomUUID();
        UUID clearingAccountId = UUID.randomUUID();

        when(cardManagementClient.getCard(cardId)).thenReturn(
                new CardResponse(cardId, accountId, UUID.randomUUID(), "•••• 1234", "Jane Doe", "DEBIT", 1, 2030,
                        "ACTIVE", new BigDecimal("2000.00"), Instant.now(), Instant.now(), null));
        when(cardAuthorizationRepository.sumApprovedAmountSince(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(accountsClient.getAccount(accountId)).thenReturn(
                new com.fintechplatform.cardauthorization.client.AccountResponse(
                        accountId, UUID.randomUUID(), ledgerAccountId, "ACC-1", "CHECKING", "USD", "ACTIVE", Instant.now()));
        when(clearingAccountService.getOrCreateClearingLedgerAccountId("USD")).thenReturn(clearingAccountId);
        when(executionService.execute(any(), eq(ledgerAccountId), eq(clearingAccountId)))
                .thenAnswer(invocation -> {
                    CardAuthorization authorization = invocation.getArgument(0);
                    authorization.markApproved(UUID.randomUUID());
                    return authorization;
                });

        CardAuthorization result = service.authorizePurchase(new AuthorizePurchaseRequest(cardId, "Coffee Shop", new BigDecimal("4.50"), "USD"));

        assertThat(result.getStatus()).isEqualTo(CardAuthorizationStatus.APPROVED);
    }
}
