package com.fintechplatform.ledger.web;

import com.fintechplatform.ledger.domain.LedgerAccount;
import com.fintechplatform.ledger.dto.LedgerAccountResponse;
import com.fintechplatform.ledger.dto.LedgerBalanceResponse;
import com.fintechplatform.ledger.dto.OpenLedgerAccountRequest;
import com.fintechplatform.ledger.dto.PostingResponse;
import com.fintechplatform.ledger.service.LedgerService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledger/accounts")
public class LedgerAccountController {

    private final LedgerService ledgerService;

    public LedgerAccountController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<LedgerAccountResponse> openAccount(@Valid @RequestBody OpenLedgerAccountRequest request) {
        LedgerAccount account = ledgerService.openAccount(request);
        return ResponseEntity.created(URI.create("/api/ledger/accounts/" + account.getId())).body(LedgerAccountResponse.from(account));
    }

    @GetMapping("/{id}")
    public LedgerAccountResponse getAccount(@PathVariable UUID id) {
        return LedgerAccountResponse.from(ledgerService.getAccount(id));
    }

    @GetMapping("/{id}/balance")
    public LedgerBalanceResponse getBalance(@PathVariable UUID id) {
        BigDecimal balance = ledgerService.getBalance(id);
        String currency = ledgerService.getAccount(id).getCurrency();
        return new LedgerBalanceResponse(id, balance, currency);
    }

    @GetMapping("/{id}/entries")
    public List<PostingResponse> getPostingHistory(@PathVariable UUID id) {
        return ledgerService.getPostingHistory(id).stream().map(PostingResponse::from).toList();
    }
}
