package dev.iamkavindu.nuledger.ledger.persistence;

import dev.iamkavindu.nuledger.ledger.persistence.entity.Account;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByCode(String code);
}
