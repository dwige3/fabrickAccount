package it.evoluta.fabrickaccount.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String TIMESTAMP = "timestamp";
    public static final String ERRORS = "errors";
    public static final String PER = " per ";
    public static final String MESSAGE = "message";
    public static final String PATH = "path";
    public static final String STATUS = "status";
    public static final String RESOURCE_NOT_FOUND = "Resource Not Found";

    @ExceptionHandler(NoPayloadReceivedException.class)
    public ResponseEntity<Object> handleNoPayloadReceivedException(NoPayloadReceivedException ex, WebRequest request) {

        log.error("No Payload Exception: " +
                ex.getLocalizedMessage() +
                PER +
                ((ServletWebRequest) request).getRequest().getRequestURI());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(ERRORS, RESOURCE_NOT_FOUND);
        body.put(MESSAGE, ex.getLocalizedMessage());
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidResponseException.class)
    public ResponseEntity<Object> handleInvalidResponseException(InvalidResponseException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(ERRORS, RESOURCE_NOT_FOUND);
        body.put(MESSAGE, ex.getLocalizedMessage());
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(ERRORS, RESOURCE_NOT_FOUND);
        body.put(MESSAGE, ex.getLocalizedMessage());
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoTransactionsFoundException.class)
    public ResponseEntity<Object> handleNoTransactionsFoundException(NoTransactionsFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(ERRORS, RESOURCE_NOT_FOUND);
        body.put(MESSAGE, ex.getLocalizedMessage());
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDate.now());
        body.put(STATUS, status.value());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put(ERRORS, errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
