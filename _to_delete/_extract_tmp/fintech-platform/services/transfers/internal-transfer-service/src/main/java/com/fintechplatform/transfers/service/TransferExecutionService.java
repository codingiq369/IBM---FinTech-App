package com.fintechplatform.transfers.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.transfers.client.AccountResponse;
import com.fintechplatform.transfers.client.JournalEntryResponse;
import com.fintechplatform.transfers.client.LedgerClient;
import com.fintechplatform.transfers.domain.Transfer;
import com.fintechplatform.transfers.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Deliberately its own bean, separate from {@link TransferService}. Spring's
 * {@code @Transactional} works by wrapping bean methods in a proxy — a call
 * from one method to another <em>on the same object</em> skips that proxy
 * entirely, silently ignoring the annotation. Putting this step on its own
 * bean means {@link TransferService} calls it through the real proxy, so
 * {@code REQUIRES_NEW} actually takes effect: the FAILED/COMPLETED update
 * below commits in its own transaction regardless of what happens around it.
 */
@Service
public class TransferExecutionService {

    private static final Logger log = LoggerFactory.getLogger(TransferExecutionService.class);

    private final TransferRepository transferRepository;
    private final LedgerClient ledgerClient;
    private final ObjectMapper objectMapper;

    public TransferExecutionService(TransferRepository transferRepository, LedgerClient ledgerClient, ObjectMapper objectMapper) {
        this.transferRepository = transferRepository;
        this.ledgerClient = ledgerClient;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transfer execute(Transfer transfer, AccountResponse source, AccountResponse destination, String description) {
        try {
            JournalEntryResponse entry = ledgerClient.postTransfer(
                    transfer.getId(), source.ledgerAccountId(), destination.ledgerAccountId(), transfer.getAmount(), description);
            transfer.markCompleted(entry.id());
        } catch (RestClientException e) {
            String reason = extractFailureReason(e);
            log.warn("Transfer {} failed while posting to the ledger: {}", transfer.getId(), reason);
            transfer.markFailed(reason);
        }
        return transferRepository.save(transfer);
    }

    /** ledger-service's error responses are shaped like
     * {@code {"status":422,"error":"...","timestamp":"..."}} (see its
     * GlobalExceptionHandler). Pull the human-readable "error" field out of
     * that body instead of surfacing the raw HTTP exception text, which
     * includes status lines and the whole JSON blob. Falls back to the
     * exception's own message for anything that isn't shaped that way
     * (e.g. ledger-service being unreachable entirely). */
    private String extractFailureReason(RestClientException e) {
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
