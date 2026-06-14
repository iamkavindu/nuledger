package dev.iamkavindu.nuledger.ledger.service.exception;

import java.util.UUID;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(UUID accountId) {
        super("Account is not ACTIVE: " + accountId);
    }
}
