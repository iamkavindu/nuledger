package dev.iamkavindu.nuledger.ledger.service.exception;

import java.util.UUID;

public class EntryAlreadyReversedException extends RuntimeException {
    public EntryAlreadyReversedException(UUID originalEntryId) {
        super("Journal entry already reversed: " + originalEntryId);
    }
}
