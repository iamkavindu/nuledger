package dev.iamkavindu.nuledger.ledger.model;

import java.util.UUID;

public record ReverseTransactionResult(UUID reversalEntryId, UUID originalEntryId, boolean replayed) {}
