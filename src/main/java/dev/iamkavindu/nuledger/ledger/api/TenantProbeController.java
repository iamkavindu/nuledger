package dev.iamkavindu.nuledger.ledger.api;

import dev.iamkavindu.nuledger.ledger.model.AccountId;
import dev.iamkavindu.nuledger.ledger.model.CreateAccountCommand;
import dev.iamkavindu.nuledger.ledger.model.JournalDirection;
import dev.iamkavindu.nuledger.ledger.model.Money;
import dev.iamkavindu.nuledger.ledger.model.PostTransactionCommand;
import dev.iamkavindu.nuledger.ledger.model.PostTransactionResult;
import dev.iamkavindu.nuledger.ledger.model.PostingLine;
import dev.iamkavindu.nuledger.ledger.model.ReverseTransactionCommand;
import dev.iamkavindu.nuledger.ledger.model.ReverseTransactionResult;
import dev.iamkavindu.nuledger.ledger.persistence.AccountRepository;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account;
import dev.iamkavindu.nuledger.ledger.service.AccountService;
import dev.iamkavindu.nuledger.ledger.service.LedgerService;
import dev.iamkavindu.nuledger.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/tenant-probe")
public class TenantProbeController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private LedgerService ledgerService;

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

    @PostMapping("/accounts/service/create")
    @Transactional
    public String createViaService(
            @RequestParam String code, @RequestParam String name, @RequestParam Account.AccountType type) {
        var id = accountService.createAccount(new CreateAccountCommand(code, name, type, false));
        return "created " + id.value();
    }

    @PostMapping("/ledger/service/post-sample")
    public PostTransactionResult postSample(@RequestParam String idempotencyKey) {
        AccountId cash = accountService.createAccount(
                new CreateAccountCommand("CASH-001", "Cash", Account.AccountType.ASSET, false));
        AccountId revenue = accountService.createAccount(
                new CreateAccountCommand("REV-001", "Revenue", Account.AccountType.REVENUE, false));

        var lines = List.of(
                new PostingLine(cash, JournalDirection.DEBIT, Money.ofMinor(10000, "USD")),
                new PostingLine(revenue, JournalDirection.CREDIT, Money.ofMinor(10000, "USD")));

        return ledgerService.postTransaction(new PostTransactionCommand(idempotencyKey, lines, "probe-sample"));
    }

    @PostMapping("/leger/service/reverse-sample")
    public ReverseTransactionResult reverseSample(
            @RequestParam UUID originalEntryId, @RequestParam String idempotencyKey) {
        return ledgerService.reverseTransaction(
                new ReverseTransactionCommand(originalEntryId, idempotencyKey, "probe-reverse"));
    }
}
