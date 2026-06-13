package dev.iamkavindu.nuledger.ledger.api;

import dev.iamkavindu.nuledger.ledger.persistence.AccountRepository;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account;
import dev.iamkavindu.nuledger.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/tenant-probe")
public class TenantProbeController {

    @Autowired
    private AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/accounts/count")
    @Transactional(readOnly = true)
    public long countAccounts() {
        return ((Number) entityManager
                        .createNativeQuery("SELECT count(*) FROM accounts")
                        .getSingleResult())
                .longValue();
    }

    @PostMapping("/accounts/seed")
    @Transactional
    public String seedAccount() {
        String tenantId = TenantContext.requireTenantId();
        entityManager.createNativeQuery("""
                        INSERT INTO accounts (tenant_id, code, name, account_type)
                        VALUES (:tenantId, 'PROBE-001', 'Probe Account', 'ASSET')
                        """).setParameter("tenantId", tenantId).executeUpdate();
        return "seeded for " + tenantId;
    }

    @GetMapping("/accounts/jpa/count")
    @Transactional(readOnly = true)
    public long jpaCount() {
        return accountRepository.count();
    }

    @PostMapping("/accounts/jpa/seed")
    @Transactional
    public String jpaSeed() {
        var account = new Account();
        account.setCode("JPA-001");
        account.setName("JPA Probe Account");
        account.setAccountType(Account.AccountType.ASSET);
        // do NOT set tenantId — Hibernate injects it via @TenantId
        accountRepository.save(account);
        return "jpa seeded for " + TenantContext.requireTenantId();
    }
}
