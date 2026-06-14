package dev.iamkavindu.nuledger.ledger.service;

import dev.iamkavindu.nuledger.ledger.model.AccountId;
import dev.iamkavindu.nuledger.ledger.model.JournalDirection;
import dev.iamkavindu.nuledger.ledger.model.Money;
import dev.iamkavindu.nuledger.ledger.model.PostTransactionCommand;
import dev.iamkavindu.nuledger.ledger.model.PostTransactionResult;
import dev.iamkavindu.nuledger.ledger.model.PostingLine;
import dev.iamkavindu.nuledger.ledger.model.ReverseTransactionCommand;
import dev.iamkavindu.nuledger.ledger.model.ReverseTransactionResult;
import dev.iamkavindu.nuledger.ledger.persistence.AccountBalanceRepository;
import dev.iamkavindu.nuledger.ledger.persistence.AccountRepository;
import dev.iamkavindu.nuledger.ledger.persistence.JournalEntryRepository;
import dev.iamkavindu.nuledger.ledger.persistence.JournalLineRepository;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account;
import dev.iamkavindu.nuledger.ledger.persistence.entity.Account.AccountStatus;
import dev.iamkavindu.nuledger.ledger.persistence.entity.AccountBalance;
import dev.iamkavindu.nuledger.ledger.persistence.entity.JournalEntry;
import dev.iamkavindu.nuledger.ledger.persistence.entity.JournalLine;
import dev.iamkavindu.nuledger.ledger.service.exception.AccountNotFoundException;
import dev.iamkavindu.nuledger.ledger.service.exception.CannotReverseEntryException;
import dev.iamkavindu.nuledger.ledger.service.exception.EntryAlreadyReversedException;
import dev.iamkavindu.nuledger.ledger.service.exception.InactiveAccountException;
import dev.iamkavindu.nuledger.ledger.service.exception.InvalidPostingException;
import dev.iamkavindu.nuledger.ledger.service.exception.JournalEntryNotFoundException;
import dev.iamkavindu.nuledger.ledger.service.exception.NegativeBalanceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final DoubleEntryValidator doubleEntryValidator;

    public LedgerService(
            JournalEntryRepository journalEntryRepository,
            JournalLineRepository journalLineRepository,
            AccountRepository accountRepository,
            AccountBalanceRepository accountBalanceRepository,
            DoubleEntryValidator doubleEntryValidator) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalLineRepository = journalLineRepository;
        this.accountRepository = accountRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.doubleEntryValidator = doubleEntryValidator;
    }

    @Transactional
    public PostTransactionResult postTransaction(PostTransactionCommand command) {
        var idempotencyKey = command.idempotencyKey().trim();

        var existing = journalEntryRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new PostTransactionResult(existing.get().getId(), true);
        }

        var lines = command.lines();
        var currency = doubleEntryValidator.validateAndResolveCurrency(lines);

        var accounts = loadAndValidateAccounts(lines);
        var projectedDeltas = new HashMap<UUID, BigDecimal>();

        for (var line : lines) {
            var accountId = line.accountId().value();
            var account = accounts.get(accountId);
            var delta = signedDelta(account, line.direction(), line.amount().toDecimal());
            projectedDeltas.merge(accountId, delta, BigDecimal::add);
        }

        for (var entry : projectedDeltas.entrySet()) {
            var accountId = entry.getKey();
            var delta = entry.getValue();
            var account = accounts.get(accountId);

            var balanceRow = accountBalanceRepository
                    .findByAccountIdAndCurrency(accountId, currency)
                    .orElseGet(() -> newBalanceRow(accountId, currency));

            var newBalance = balanceRow.getBalance().add(delta);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0 && !account.isAllowNegative()) {
                throw new NegativeBalanceException(accountId, currency);
            }
            balanceRow.setBalance(newBalance);
            accountBalanceRepository.save(balanceRow);
        }

        var entry = new JournalEntry();
        entry.setIdempotencyKey(idempotencyKey);
        entry.setCorrelationId(command.correlationId());
        entry.setStatus(JournalEntry.EntryStatus.POSTED);
        journalEntryRepository.save(entry);

        var order = 1;
        for (var line : lines) {
            var journalLine = new JournalLine();
            journalLine.setEntryId(entry.getId());
            journalLine.setAccountId(line.accountId().value());
            journalLine.setDirection(toLineDirection(line.direction()));
            journalLine.setAmount(line.amount().toDecimal());
            journalLine.setCurrency(currency);
            journalLine.setLineOrder(order++);
            journalLineRepository.save(journalLine);
        }

        return new PostTransactionResult(entry.getId(), false);
    }

    private Map<UUID, Account> loadAndValidateAccounts(List<PostingLine> lines) {
        var accounts = new HashMap<UUID, Account>();
        for (var line : lines) {
            var accountId = line.accountId().value();
            accounts.computeIfAbsent(accountId, id -> {
                var account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
                if (account.getStatus() != AccountStatus.ACTIVE) {
                    throw new InactiveAccountException(id);
                }
                return account;
            });
        }
        return accounts;
    }

    private AccountBalance newBalanceRow(UUID accountId, String currency) {
        var row = new AccountBalance();
        row.setAccountId(accountId);
        row.setCurrency(currency);
        row.setBalance(BigDecimal.ZERO);
        return row;
    }

    private BigDecimal signedDelta(Account account, JournalDirection direction, BigDecimal amount) {
        var debitIncrease = account.getAccountType() == Account.AccountType.ASSET
                || account.getAccountType() == Account.AccountType.EXPENSE;

        if (debitIncrease) {
            return direction == JournalDirection.DEBIT ? amount : amount.negate();
        }
        return direction == JournalDirection.CREDIT ? amount : amount.negate();
    }

    private JournalLine.LineDirection toLineDirection(JournalDirection direction) {
        return JournalLine.LineDirection.valueOf(direction.name());
    }

    @Transactional
    public ReverseTransactionResult reverseTransaction(ReverseTransactionCommand command) {
        var idempotencyKey = command.idempotencyKey().trim();

        var existingReversal = journalEntryRepository.findByIdempotencyKey(idempotencyKey);
        if (existingReversal.isPresent()) {
            var reversal = existingReversal.get();
            return new ReverseTransactionResult(reversal.getId(), reversal.getReversesEntryId(), true);
        }

        var original = journalEntryRepository
                .findById(command.originalEntryId())
                .orElseThrow(() -> new JournalEntryNotFoundException(command.originalEntryId()));

        if (original.getReversesEntryId() != null) {
            throw new CannotReverseEntryException(original.getId());
        }

        if (journalEntryRepository.findByReversesEntryId(original.getId()).isPresent()) {
            throw new EntryAlreadyReversedException(original.getId());
        }

        var originalLines = journalLineRepository.findByEntryIdOrderByLineOrderAsc(original.getId());
        if (originalLines.size() < 2) {
            throw new InvalidPostingException("Original entry has no lines to reverse");
        }

        var contraLines = originalLines.stream().map(this::toContraPostingLine).toList();

        var reversalEntryId = postBalancedLines(idempotencyKey, contraLines, command.correlationId(), original.getId());

        return new ReverseTransactionResult(reversalEntryId, original.getId(), false);
    }

    private UUID postBalancedLines(
            String idempotencyKey, List<PostingLine> lines, String correlationId, UUID reversesEntryId) {
        var currency = doubleEntryValidator.validateAndResolveCurrency(lines);

        var accounts = loadAndValidateAccounts(lines);
        var projectedDeltas = new HashMap<UUID, BigDecimal>();

        for (var line : lines) {
            var accountId = line.accountId().value();
            var account = accounts.get(accountId);
            var delta = signedDelta(account, line.direction(), line.amount().toDecimal());
            projectedDeltas.merge(accountId, delta, BigDecimal::add);
        }
        for (var entry : projectedDeltas.entrySet()) {
            var accountId = entry.getKey();
            var delta = entry.getValue();
            var account = accounts.get(accountId);
            var balanceRow = accountBalanceRepository
                    .findByAccountIdAndCurrency(accountId, currency)
                    .orElseGet(() -> newBalanceRow(accountId, currency));
            var newBalance = balanceRow.getBalance().add(delta);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0 && !account.isAllowNegative()) {
                throw new NegativeBalanceException(accountId, currency);
            }
            balanceRow.setBalance(newBalance);
            accountBalanceRepository.save(balanceRow);
        }

        var entry = new JournalEntry();
        entry.setIdempotencyKey(idempotencyKey);
        entry.setCorrelationId(correlationId);
        entry.setReversesEntryId(reversesEntryId);
        entry.setStatus(JournalEntry.EntryStatus.POSTED);
        journalEntryRepository.save(entry);

        var order = 1;
        for (var line : lines) {
            var journalLine = new JournalLine();
            journalLine.setEntryId(entry.getId());
            journalLine.setAccountId(line.accountId().value());
            journalLine.setDirection(toLineDirection(line.direction()));
            journalLine.setAmount(line.amount().toDecimal());
            journalLine.setCurrency(currency);
            journalLine.setLineOrder(order++);
            journalLineRepository.save(journalLine);
        }
        return entry.getId();
    }

    private PostingLine toContraPostingLine(JournalLine line) {
        var flipped = line.getDirection() == JournalLine.LineDirection.DEBIT
                ? JournalDirection.CREDIT
                : JournalDirection.DEBIT;

        var money = Money.fromDecimal(line.getAmount(), line.getCurrency().trim());

        return new PostingLine(AccountId.of(line.getAccountId()), flipped, money);
    }
}
