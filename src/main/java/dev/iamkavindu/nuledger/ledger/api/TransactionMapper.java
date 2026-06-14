package dev.iamkavindu.nuledger.ledger.api;

import dev.iamkavindu.nuledger.ledger.api.dto.PostTransactionRequest;
import dev.iamkavindu.nuledger.ledger.api.dto.PostingLineRequest;
import dev.iamkavindu.nuledger.ledger.api.dto.ReverseTransactionRequest;
import dev.iamkavindu.nuledger.ledger.api.dto.ReverseTransactionResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.TransactionResponse;
import dev.iamkavindu.nuledger.ledger.model.AccountId;
import dev.iamkavindu.nuledger.ledger.model.Money;
import dev.iamkavindu.nuledger.ledger.model.PostTransactionCommand;
import dev.iamkavindu.nuledger.ledger.model.PostTransactionResult;
import dev.iamkavindu.nuledger.ledger.model.PostingLine;
import dev.iamkavindu.nuledger.ledger.model.ReverseTransactionCommand;
import dev.iamkavindu.nuledger.ledger.model.ReverseTransactionResult;
import java.util.UUID;

public final class TransactionMapper {

    private TransactionMapper() {}

    public static PostTransactionCommand toCommand(PostTransactionRequest request) {
        var lines =
                request.lines().stream().map(TransactionMapper::toPostingLine).toList();
        return new PostTransactionCommand(request.idempotencyKey(), lines, request.correlationId());
    }

    private static PostingLine toPostingLine(PostingLineRequest line) {
        return new PostingLine(
                AccountId.of(line.accountId()), line.direction(), Money.ofMinor(line.amountMinor(), line.currency()));
    }

    public static TransactionResponse toResponse(PostTransactionResult result) {
        return new TransactionResponse(result.entryId(), result.replayed());
    }

    public static ReverseTransactionCommand toCommand(UUID originalEntryId, ReverseTransactionRequest request) {
        return new ReverseTransactionCommand(originalEntryId, request.idempotencyKey(), request.correlationId());
    }

    public static ReverseTransactionResponse toResponse(ReverseTransactionResult result) {
        return new ReverseTransactionResponse(result.reversalEntryId(), result.originalEntryId(), result.replayed());
    }
}
