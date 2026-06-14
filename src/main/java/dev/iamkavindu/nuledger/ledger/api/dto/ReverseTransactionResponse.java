package dev.iamkavindu.nuledger.ledger.api.dto;

import java.util.UUID;

public record ReverseTransactionResponse(UUID reversalEntryId, UUID originalEntryId, boolean replayed) {}
