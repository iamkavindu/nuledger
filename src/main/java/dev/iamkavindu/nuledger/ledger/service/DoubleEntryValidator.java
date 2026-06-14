package dev.iamkavindu.nuledger.ledger.service;

import dev.iamkavindu.nuledger.ledger.model.JournalDirection;
import dev.iamkavindu.nuledger.ledger.model.PostingLine;
import dev.iamkavindu.nuledger.ledger.service.exception.InvalidPostingException;
import dev.iamkavindu.nuledger.ledger.service.exception.UnbalancedEntryException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DoubleEntryValidator {

    public String validateAndResolveCurrency(List<PostingLine> lines) {
        String currency = null;
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;

        for (var line : lines) {
            var lineCurrency = line.amount().currency();
            if (currency == null) {
                currency = lineCurrency;
            } else if (!currency.equals(lineCurrency)) {
                throw new InvalidPostingException("All lines must use the same currency in *MVP*");
            }

            var amount = line.amount().toDecimal();
            if (line.direction() == JournalDirection.DEBIT) {
                debits = debits.add(amount);
            } else {
                credits = credits.add(amount);
            }
        }

        if (debits.compareTo(credits) != 0) {
            throw new UnbalancedEntryException();
        }
        return currency;
    }
}
