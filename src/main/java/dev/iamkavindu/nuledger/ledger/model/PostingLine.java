package dev.iamkavindu.nuledger.ledger.model;

public record PostingLine(AccountId accountId, JournalDirection direction, Money amount) {
    public PostingLine {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }
    }
}
