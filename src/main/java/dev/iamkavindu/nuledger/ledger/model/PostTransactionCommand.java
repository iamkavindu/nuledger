package dev.iamkavindu.nuledger.ledger.model;

import java.util.List;

public record PostTransactionCommand(String idempotencyKey, List<PostingLine> lines, String correlationId) {
    public PostTransactionCommand {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }
        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("idempotencyKey must be at most 128 characters");
        }
        if (lines == null || lines.size() < 2) {
            throw new IllegalArgumentException("at least 2 posting lines required");
        }
        lines = List.copyOf(lines);
    }
}
