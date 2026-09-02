package com.fintechplatform.cardauthorization.client;

import java.util.List;

public record PostJournalEntryRequest(String description, String transactionReference, List<PostingRequest> postings) {}
