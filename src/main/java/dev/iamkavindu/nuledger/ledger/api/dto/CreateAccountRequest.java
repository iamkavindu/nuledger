package dev.iamkavindu.nuledger.ledger.api.dto;

import dev.iamkavindu.nuledger.ledger.persistence.entity.Account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank String name,
        @NotNull AccountType accountType,
        boolean allowNegative) {}
