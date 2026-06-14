package dev.iamkavindu.nuledger.ledger.service.exception;

public class DuplicateAccountCodeException extends RuntimeException {
    public DuplicateAccountCodeException(String code) {
        super("Account code already exists: " + code);
    }
}
