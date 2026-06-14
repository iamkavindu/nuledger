package dev.iamkavindu.nuledger.ledger.service.exception;

public class InvalidPostingException extends RuntimeException {
    public InvalidPostingException(String message) {
        super(message);
    }
}
