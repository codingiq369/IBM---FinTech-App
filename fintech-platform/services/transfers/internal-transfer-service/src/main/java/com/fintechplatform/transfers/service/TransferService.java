package com.fintechplatform.transfers.service;

import com.fintechplatform.transfers.client.AccountResponse;
import com.fintechplatform.transfers.client.AccountsClient;
import com.fintechplatform.transfers.domain.Transfer;
import com.fintechplatform.transfers.domain.TransferStatus;
import com.fintechplatform.transfers.dto.InitiateTransferRequest;
import com.fintechplatform.transfers.event.TransferEventPublisher;
import com.fintechplatform.transfers.repository.TransferRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountsClient accountsClient;
    private final TransferExecutionService transferExecutionService;
    private final TransferEventPublisher transferEventPublisher;

    public TransferService(
            TransferRepository transferRepository,
            AccountsClient accountsClient,
            TransferExecutionService transferExecutionService,
            TransferEventPublisher transferEventPublisher) {
        this.transferRepository = transferRepository;
        this.accountsClient = accountsClient;
        this.transferExecutionService = transferExecutionService;
        this.transferEventPublisher = transferEventPublisher;
    }

    /**
     * The flow, deliberately in three phases:
     * <ol>
     *   <li>Validate — resolve both accounts, check they're active and share
     *       a currency. Nothing is persisted yet; a failure here means the
     *       request was invalid, not that a transfer was attempted.</li>
     *   <li>Record intent — save a PENDING Transfer. From this point on,
     *       there is an audit trail no matter what happens next.</li>
     *   <li>Execute — hand off to {@link TransferExecutionService}, which
     *       asks ledger-service to post the balanced journal entry. Success
     *       moves the Transfer to COMPLETED; any failure (insufficient
     *       funds, ledger-service unreachable) moves it to FAILED with a
     *       reason, rather than throwing the record away. Only a COMPLETED
     *       outcome is published as a {@code TransferCompleted} event via
     *       {@link TransferEventPublisher} — a FAILED transfer is a
     *       successfully recorded API response, not something downstream
     *       consumers (notifications, today) need to react to.</li>
     * </ol>
     * This is a simplified stand-in for the real distributed-transaction
     * problem (a saga, an outbox, a reconciliation job) — enough to show
     * the shape without building the full machinery.
     */
    public Transfer initiateTransfer(InitiateTransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new InvalidTransferException("Cannot transfer from an account to itself");
        }

        AccountResponse source = accountsClient.getAccount(request.sourceAccountId());
        AccountResponse destination = accountsClient.getAccount(request.destinationAccountId());

        if (!source.isActive()) {
            throw new InvalidTransferException("Source account " + source.id() + " is not active");
        }
        if (!destination.isActive()) {
            throw new InvalidTransferException("Destination account " + destination.id() + " is not active");
        }
        if (!source.currency().equals(destination.currency())) {
            throw new InvalidTransferException(
                    "Currency mismatch: source is " + source.currency() + ", destination is " + destination.currency()
                            + " (cross-currency transfers aren't supported in this slice)");
        }

        Transfer transfer = new Transfer(source.id(), destination.id(), request.amount(), source.currency());
        transfer = transferRepository.save(transfer);

        Transfer result = transferExecutionService.execute(transfer, source, destination, request.descriptionOrDefault());
        if (result.getStatus() == TransferStatus.COMPLETED) {
            transferEventPublisher.publishTransferCompleted(result);
        }
        return result;
    }

    public Transfer getById(UUID id) {
        return transferRepository.findById(id).orElseThrow(() -> new TransferNotFoundException(id));
    }

    public List<Transfer> getByAccountId(UUID accountId) {
        return transferRepository.findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(accountId, accountId);
    }
}
