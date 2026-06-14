package dev.iamkavindu.nuledger.ledger.service.exception;

import java.util.UUID;

public class NegativeBalanceException extends RuntimeException {
    public NegativeBalanceException(UUID accountId, String currency) {
        super("Negative balance not allowed for account " + accountId + " (" + currency + ")");
    }
}
