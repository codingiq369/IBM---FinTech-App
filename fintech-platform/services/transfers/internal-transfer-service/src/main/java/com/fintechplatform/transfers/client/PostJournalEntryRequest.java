package com.fintechplatform.transfers.client;

import java.util.List;

public record PostJournalEntryRequest(String description, String transactionReference, List<PostingRequest> postings) {}
