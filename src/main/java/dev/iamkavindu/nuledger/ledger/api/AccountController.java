package dev.iamkavindu.nuledger.ledger.api;

import dev.iamkavindu.nuledger.ledger.api.dto.AccountBalancesResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.AccountResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.CreateAccountRequest;
import dev.iamkavindu.nuledger.ledger.model.AccountId;
import dev.iamkavindu.nuledger.ledger.service.AccountService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/{version}/accounts", version = "v1")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        var accountId = accountService.createAccount(AccountMapper.toCommand(request));
        var account = accountService.requireAccount(accountId);
        var body = AccountMapper.toResponse(account);
        return ResponseEntity.created(APIUtil.locationHeader("/{id}", body.id()))
                .body(body);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        var account = accountService.requireAccount(AccountId.of(id));
        return ResponseEntity.ok(AccountMapper.toResponse(account));
    }

    @GetMapping(params = "code")
    public ResponseEntity<AccountResponse> getByCode(@RequestParam String code) {
        var account = accountService.requireAccountByCode(code);
        return ResponseEntity.ok(AccountMapper.toResponse(account));
    }

    @GetMapping("/{id}/balances")
    public ResponseEntity<AccountBalancesResponse> getBalances(@PathVariable UUID id) {
        var accountId = AccountId.of(id);
        var rows = accountService.getBalancesForAccount(accountId);
        return ResponseEntity.ok(AccountMapper.toBalancesResponse(id, rows));
    }
}
