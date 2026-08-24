package com.fintechplatform.accounts.client;

import java.util.UUID;

public record OpenLedgerAccountRequest(UUID ownerReference, String currency) {}
