package dev.iamkavindu.nuledger.ledger.service;

import dev.iamkavindu.nuledger.ledger.model.AccountId;
import dev.iamkavindu.nuledger.ledger.model.CreateAccountCommand;
import dev.iamkavindu.nuledger.ledger.persistence.AccountRepository;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account;
import dev.iamkavindu.nuledger.ledger.service.exception.AccountNotFoundException;
import dev.iamkavindu.nuledger.ledger.service.exception.DuplicateAccountCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountId createAccount(CreateAccountCommand command) {
        var code = command.code().trim();

        if (accountRepository.findByCode(code).isPresent()) {
            throw new DuplicateAccountCodeException(code);
        }

        var account = new Account();
        account.setCode(code);
        account.setName(command.name().trim());
        account.setAccountType(command.accountType());
        account.setAllowNegative(command.allowNegative());
        account.setStatus(Account.AccountStatus.ACTIVE);

        accountRepository.save(account);
        return AccountId.of(account.getId());
    }

    @Transactional(readOnly = true)
    public Account requireAccount(AccountId accountId) {
        return accountRepository
                .findById(accountId.value())
                .orElseThrow(() -> new AccountNotFoundException(accountId.value()));
    }

    @Transactional(readOnly = true)
    public Account requireAccountByCode(String code) {
        return accountRepository.findByCode(code.trim()).orElseThrow(() -> new AccountNotFoundException(code));
    }
}
