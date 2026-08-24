package com.fintechplatform.ledger.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PostJournalEntryRequest(
        @NotBlank(message = "description is required") String description,
        String transactionReference,
        @NotEmpty(message = "postings must not be empty") @Valid List<PostingRequest> postings) {}
