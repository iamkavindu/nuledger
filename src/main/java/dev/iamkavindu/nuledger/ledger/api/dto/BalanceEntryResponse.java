package dev.iamkavindu.nuledger.ledger.api.dto;

import java.time.Instant;

public record BalanceEntryResponse(String currency, long amountMinor, Instant updatedAt) {}
