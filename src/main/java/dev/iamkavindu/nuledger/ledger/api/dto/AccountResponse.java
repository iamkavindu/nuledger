package dev.iamkavindu.nuledger.ledger.api.dto;

import dev.iamkavindu.nuledger.ledger.persistence.entity.Account.AccountStatus;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account.AccountType;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String code,
        String name,
        AccountType accountType,
        boolean allowNegative,
        AccountStatus status,
        Instant createdAt) {}
