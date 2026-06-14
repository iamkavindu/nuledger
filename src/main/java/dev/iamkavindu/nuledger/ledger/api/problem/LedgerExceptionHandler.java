package dev.iamkavindu.nuledger.ledger.api.problem;

import dev.iamkavindu.nuledger.ledger.service.exception.AccountNotFoundException;
import dev.iamkavindu.nuledger.ledger.service.exception.CannotReverseEntryException;
import dev.iamkavindu.nuledger.ledger.service.exception.DuplicateAccountCodeException;
import dev.iamkavindu.nuledger.ledger.service.exception.EntryAlreadyReversedException;
import dev.iamkavindu.nuledger.ledger.service.exception.InactiveAccountException;
import dev.iamkavindu.nuledger.ledger.service.exception.InvalidPostingException;
import dev.iamkavindu.nuledger.ledger.service.exception.JournalEntryNotFoundException;
import dev.iamkavindu.nuledger.ledger.service.exception.NegativeBalanceException;
import dev.iamkavindu.nuledger.ledger.service.exception.UnbalancedEntryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "dev.iamkavindu.nuledger.ledger.api")
public class LedgerExceptionHandler {

    @ExceptionHandler(DuplicateAccountCodeException.class)
    public ProblemDetail duplicateAccount(DuplicateAccountCodeException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate account code");
        problemDetail.setType(ProblemTypes.of("duplicate-account-code"));
        return problemDetail;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail accountNotFound(AccountNotFoundException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Account not found");
        problemDetail.setType(ProblemTypes.of("account-not-found"));
        return problemDetail;
    }

    @ExceptionHandler(JournalEntryNotFoundException.class)
    public ProblemDetail journalNotFound(JournalEntryNotFoundException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Journal entry not found");
        problemDetail.setType(ProblemTypes.of("journal-entry-not-found"));
        return problemDetail;
    }

    @ExceptionHandler({UnbalancedEntryException.class, InvalidPostingException.class})
    public ProblemDetail invalidPosting(RuntimeException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problemDetail.setTitle("Invalid posting");
        problemDetail.setType(ProblemTypes.of("invalid-posting"));
        return problemDetail;
    }

    @ExceptionHandler(NegativeBalanceException.class)
    public ProblemDetail negativeBalance(NegativeBalanceException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problemDetail.setTitle("Insufficient balance");
        problemDetail.setType(ProblemTypes.of("insufficient-balance"));
        return problemDetail;
    }

    @ExceptionHandler(EntryAlreadyReversedException.class)
    public ProblemDetail alreadyReversed(EntryAlreadyReversedException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Entry already reversed");
        problemDetail.setType(ProblemTypes.of("entry-already-reversed"));
        return problemDetail;
    }

    @ExceptionHandler(CannotReverseEntryException.class)
    public ProblemDetail cannotReverse(CannotReverseEntryException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problemDetail.setTitle("Cannot reverse entry");
        problemDetail.setType(ProblemTypes.of("cannot-reverse-entry"));
        return problemDetail;
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ProblemDetail inactiveAccount(InactiveAccountException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problemDetail.setTitle("Inactive account");
        problemDetail.setType(ProblemTypes.of("inactive-account"));
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad request");
        problemDetail.setType(ProblemTypes.of("bad-request"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        var detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

        problemDetail.setTitle("Validation failed");
        problemDetail.setType(ProblemTypes.of("validation-failed"));
        return problemDetail;
    }

    @ExceptionHandler(InvalidApiVersionException.class)
    public ProblemDetail invalidApiVersion(InvalidApiVersionException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Invalid API version");
        pd.setType(ProblemTypes.of("invalid-api-version"));
        return pd;
    }
}
