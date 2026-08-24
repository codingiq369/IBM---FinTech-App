package com.fintechplatform.accounts.web;

import com.fintechplatform.accounts.service.AccountNotFoundException;
import com.fintechplatform.accounts.service.CustomerNotApprovedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(AccountNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CustomerNotApprovedException.class)
    public ResponseEntity<Object> handleNotApproved(CustomerNotApprovedException ex) {
        return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /** A downstream service (customer-service or ledger-service) returned an
     * error or is unreachable. Surfaced as 502 so callers can tell "this
     * request was bad" apart from "something upstream broke". */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Object> handleUpstreamFailure(RestClientResponseException ex) {
        return errorResponse(HttpStatus.BAD_GATEWAY, "Upstream service call failed: " + ex.getMessage());
    }

    /** A downstream service didn't respond at all (connection refused, DNS
     * failure, timeout) rather than answering with an error status. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Object> handleUpstreamUnreachable(ResourceAccessException ex) {
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "A required upstream service is unreachable: " + ex.getMessage());
    }

    private ResponseEntity<Object> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
