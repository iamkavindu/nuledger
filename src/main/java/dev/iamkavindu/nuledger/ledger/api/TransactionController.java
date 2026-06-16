package dev.iamkavindu.nuledger.ledger.api;

import dev.iamkavindu.nuledger.ledger.api.dto.PostTransactionRequest;
import dev.iamkavindu.nuledger.ledger.api.dto.ReverseTransactionRequest;
import dev.iamkavindu.nuledger.ledger.api.dto.ReverseTransactionResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.TransactionResponse;
import dev.iamkavindu.nuledger.ledger.service.LedgerService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/{version}/transactions", version = "v1")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> post(@Valid @RequestBody PostTransactionRequest request) {

        var result = ledgerService.postTransaction(TransactionMapper.toCommand(request));

        var body = TransactionMapper.toResponse(result);

        if (result.replayed()) {
            return ResponseEntity.ok(body);
        }

        return ResponseEntity.created(APIUtil.locationHeader("/{entryId}", body.entryId()))
                .body(body);
    }

    @PostMapping("/{entryId}/reverse")
    public ResponseEntity<ReverseTransactionResponse> reverse(
            @PathVariable UUID entryId, @Valid @RequestBody ReverseTransactionRequest request) {
        var result = ledgerService.reverseTransaction(TransactionMapper.toCommand(entryId, request));
        var body = TransactionMapper.toResponse(result);
        if (result.replayed()) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.created(APIUtil.locationHeader("/{reversalEntryId}", body.reversalEntryId()))
                .body(body);
    }
}
