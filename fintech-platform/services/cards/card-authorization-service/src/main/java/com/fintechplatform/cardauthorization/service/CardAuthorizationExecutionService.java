package com.fintechplatform.cardauthorization.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.cardauthorization.client.JournalEntryResponse;
import com.fintechplatform.cardauthorization.client.LedgerClient;
import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import com.fintechplatform.cardauthorization.repository.CardAuthorizationRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Deliberately its own bean, separate from {@link CardAuthorizationService},
 * for exactly the reason {@code TransferExecutionService} in
 * transfers-service is: {@code @Transactional(propagation = REQUIRES_NEW)}
 * only takes effect through Spring's proxy, which a same-object method call
 * bypasses. Putting the ledger call and the resulting APPROVED/DECLINED
 * write on its own bean means that write commits in its own transaction no
 * matter what happens in the caller.
 */
@Service
public class CardAuthorizationExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CardAuthorizationExecutionService.class);

    private final CardAuthorizationRepository cardAuthorizationRepository;
    private final LedgerClient ledgerClient;
    private final ObjectMapper objectMapper;

    public CardAuthorizationExecutionService(
            CardAuthorizationRepository cardAuthorizationRepository, LedgerClient ledgerClient, ObjectMapper objectMapper) {
        this.cardAuthorizationRepository = cardAuthorizationRepository;
        this.ledgerClient = ledgerClient;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CardAuthorization execute(CardAuthorization authorization, UUID cardholderLedgerAccountId, UUID clearingLedgerAccountId) {
        try {
            JournalEntryResponse entry = ledgerClient.postCardPurchase(
                    authorization.getId(),
                    cardholderLedgerAccountId,
                    clearingLedgerAccountId,
                    authorization.getAmount(),
                    "Card purchase at " + authorization.getMerchantName());
            authorization.markApproved(entry.id());
        } catch (RestClientException e) {
            String reason = extractDeclineReason(e);
            log.info("Card authorization {} declined by the ledger: {}", authorization.getId(), reason);
            authorization.markDeclined(reason);
        }
        return cardAuthorizationRepository.save(authorization);
    }

    /** ledger-service's error responses are shaped like
     * {@code {"status":422,"error":"...","timestamp":"..."}} (see its
     * GlobalExceptionHandler) — pull the human-readable "error" field out
     * instead of surfacing the raw HTTP exception dump. Falls back to the
     * exception's own message when the body isn't shaped that way (e.g.
     * ledger-service being unreachable entirely). */
    private String extractDeclineReason(RestClientException e) {
        if (e instanceof RestClientResponseException responseException) {
            try {
                JsonNode body = objectMapper.readTree(responseException.getResponseBodyAsByteArray());
                if (body.hasNonNull("error")) {
                    return body.get("error").asText();
                }
            } catch (Exception parseFailure) {
                // Fall through to the generic message below.
            }
        }
        return e.getMessage();
    }
}
