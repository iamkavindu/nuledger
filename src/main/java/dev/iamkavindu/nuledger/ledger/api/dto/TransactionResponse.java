package dev.iamkavindu.nuledger.ledger.api.dto;

import java.util.UUID;

public record TransactionResponse(UUID entryId, boolean replayed) {}
