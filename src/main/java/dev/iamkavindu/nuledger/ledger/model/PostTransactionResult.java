package dev.iamkavindu.nuledger.ledger.model;

import java.util.UUID;

public record PostTransactionResult(UUID entryId, boolean replayed) {}
