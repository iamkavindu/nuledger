package dev.iamkavindu.nuledger.ledger.api.dto;

import java.util.List;
import java.util.UUID;

public record AccountBalancesResponse(UUID accountId, List<BalanceEntryResponse> balances) {}
