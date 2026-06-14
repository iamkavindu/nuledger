package dev.iamkavindu.nuledger.ledger.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PostTransactionRequest(
        @NotBlank @Size(max = 128) String idempotencyKey,
        String correlationId,
        @NotNull @Size(min = 2) @Valid List<PostingLineRequest> lines) {}
