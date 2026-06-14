package dev.iamkavindu.nuledger.ledger.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseTransactionRequest(
        @NotBlank @Size(max = 128) String idempotencyKey, String correlationId) {}
