package dev.iamkavindu.nuledger.ledger.service.exception;

import java.util.UUID;

public class CannotReverseEntryException extends RuntimeException {
    public CannotReverseEntryException(UUID entryId) {
        super("cannot reverse a reversal entry: " + entryId);
    }
}
