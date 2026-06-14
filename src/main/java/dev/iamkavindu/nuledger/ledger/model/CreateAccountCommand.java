package dev.iamkavindu.nuledger.ledger.model;

import dev.iamkavindu.nuledger.ledger.persistence.entity.Account.AccountType;

public record CreateAccountCommand(String code, String name, AccountType accountType, boolean allowNegative) {
    public CreateAccountCommand {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (code.length() > 64) {
            throw new IllegalArgumentException("code must be at most 64 characters");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
    }
}
