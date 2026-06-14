package dev.iamkavindu.nuledger.ledger.service.exception;

public class UnbalancedEntryException extends RuntimeException {
    public UnbalancedEntryException() {
        super("Debits and credits must balance for a single currency");
    }
}
