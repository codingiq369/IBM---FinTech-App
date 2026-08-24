package com.fintechplatform.ledger.web;

import com.fintechplatform.ledger.domain.JournalEntry;
import com.fintechplatform.ledger.dto.JournalEntryResponse;
import com.fintechplatform.ledger.dto.PostJournalEntryRequest;
import com.fintechplatform.ledger.service.LedgerService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledger/journal-entries")
public class JournalEntryController {

    private final LedgerService ledgerService;

    public JournalEntryController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<JournalEntryResponse> postJournalEntry(@Valid @RequestBody PostJournalEntryRequest request) {
        JournalEntry entry = ledgerService.postJournalEntry(request);
        return ResponseEntity.created(URI.create("/api/ledger/journal-entries/" + entry.getId())).body(JournalEntryResponse.from(entry));
    }
}
