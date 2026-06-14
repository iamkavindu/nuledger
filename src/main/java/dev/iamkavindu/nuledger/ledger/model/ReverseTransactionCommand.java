package dev.iamkavindu.nuledger.ledger.model;

import java.util.UUID;

public record ReverseTransactionCommand(UUID originalEntryId, String idempotencyKey, String correlationId) {
    public ReverseTransactionCommand {
        if (originalEntryId == null) {
            throw new IllegalArgumentException("originalEntryId must not be null");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }
        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("idempotencyKey must be at most 128 characters");
        }
    }
}
