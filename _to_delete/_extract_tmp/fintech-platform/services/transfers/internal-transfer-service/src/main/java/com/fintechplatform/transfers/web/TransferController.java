package com.fintechplatform.transfers.web;

import com.fintechplatform.transfers.domain.Transfer;
import com.fintechplatform.transfers.dto.InitiateTransferRequest;
import com.fintechplatform.transfers.dto.TransferResponse;
import com.fintechplatform.transfers.service.TransferService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Returns 201 whether the transfer ended up COMPLETED or FAILED — the
     * request to attempt a transfer succeeded either way, and the body's
     * {@code status} field is how the caller finds out which. A 4xx here
     * means the request itself was invalid (bad account, currency
     * mismatch); it never means "the transfer failed".
     */
    @PostMapping
    public ResponseEntity<TransferResponse> initiateTransfer(@Valid @RequestBody InitiateTransferRequest request) {
        Transfer transfer = transferService.initiateTransfer(request);
        return ResponseEntity.created(URI.create("/api/transfers/" + transfer.getId())).body(TransferResponse.from(transfer));
    }

    @GetMapping("/{id}")
    public TransferResponse getById(@PathVariable UUID id) {
        return TransferResponse.from(transferService.getById(id));
    }

    @GetMapping
    public List<TransferResponse> getByAccount(@RequestParam UUID accountId) {
        return transferService.getByAccountId(accountId).stream().map(TransferResponse::from).toList();
    }
}
