package it.evoluta.fabrickaccount.exception;

public class UnexpectedApiException extends RuntimeException {
    public UnexpectedApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
