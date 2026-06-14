package dev.iamkavindu.nuledger.ledger.api;

import dev.iamkavindu.nuledger.ledger.api.dto.AccountBalancesResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.AccountResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.BalanceEntryResponse;
import dev.iamkavindu.nuledger.ledger.api.dto.CreateAccountRequest;
import dev.iamkavindu.nuledger.ledger.model.CreateAccountCommand;
import dev.iamkavindu.nuledger.ledger.model.Money;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account;
import dev.iamkavindu.nuledger.ledger.persistence.entity.AccountBalance;
import java.util.List;
import java.util.UUID;

public final class AccountMapper {
    private AccountMapper() {}

    public static CreateAccountCommand toCommand(CreateAccountRequest request) {
        return new CreateAccountCommand(request.code(), request.name(), request.accountType(), request.allowNegative());
    }

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCode(),
                account.getName(),
                account.getAccountType(),
                account.isAllowNegative(),
                account.getStatus(),
                account.getCreatedAt());
    }

    public static AccountBalancesResponse toBalancesResponse(UUID accountId, List<AccountBalance> rows) {
        var entries = rows.stream().map(AccountMapper::toBalanceEntry).toList();
        return new AccountBalancesResponse(accountId, entries);
    }

    private static BalanceEntryResponse toBalanceEntry(AccountBalance row) {
        var money = Money.fromDecimal(row.getBalance(), row.getCurrency().trim());
        return new BalanceEntryResponse(money.currency(), money.amountMinor(), row.getUpdatedAt());
    }
}
