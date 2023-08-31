package it.evoluta.fabrickaccount.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoPayloadReceivedException extends RuntimeException {
    public NoPayloadReceivedException(String message) {
        super(message);
    }
}
