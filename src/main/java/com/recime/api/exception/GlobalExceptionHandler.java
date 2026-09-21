package com.recime.api.exception;

import com.recime.api.config.TraceIdFilter;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CONCURRENT_UPDATE_MESSAGE = "Recipe was updated concurrently. Retry the request.";
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation Error", "Request validation failed.");
        body.put("details", details);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", "Request body is missing or malformed.");
    }

    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class,
            PessimisticLockException.class,
            LockTimeoutException.class
    })
    public ResponseEntity<Map<String, Object>> handleLockConflicts(RuntimeException ex) {
        log.warn("Concurrent recipe update rejected: {}", ex.getMessage());
        return error(HttpStatus.CONFLICT, "Conflict", CONCURRENT_UPDATE_MESSAGE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "Conflict", "Recipe data conflicts with existing records. Retry the request.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Retry the request.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String error, String message) {
        return new ResponseEntity<>(baseBody(status, error, message), status);
    }

    private Map<String, Object> baseBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("traceId", MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
        return body;
    }
}
