package dev.iamkavindu.nuledger.ledger.service.exception;

import java.util.UUID;

public class JournalEntryNotFoundException extends RuntimeException {
    public JournalEntryNotFoundException(UUID entryId) {
        super("Journal entry not found: " + entryId);
    }
}
