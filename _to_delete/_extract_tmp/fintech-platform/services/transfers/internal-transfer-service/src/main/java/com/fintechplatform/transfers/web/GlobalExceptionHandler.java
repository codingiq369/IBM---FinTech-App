package com.fintechplatform.transfers.web;

import com.fintechplatform.transfers.service.AccountNotFoundException;
import com.fintechplatform.transfers.service.InvalidTransferException;
import com.fintechplatform.transfers.service.TransferNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(AccountNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(TransferNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<Object> handleInvalid(InvalidTransferException ex) {
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
