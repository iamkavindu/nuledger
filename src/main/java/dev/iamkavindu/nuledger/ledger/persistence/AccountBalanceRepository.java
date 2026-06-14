package dev.iamkavindu.nuledger.ledger.persistence;

import dev.iamkavindu.nuledger.ledger.persistence.entity.AccountBalance;
import dev.iamkavindu.nuledger.ledger.persistence.entity.AccountBalance.AccountBalanceId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, AccountBalanceId> {
    Optional<AccountBalance> findByAccountIdAndCurrency(UUID accountId, String currency);
}
