package com.rmos.controller.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex) {
        logger.warn("Concurrency conflict prevented dirty mutation: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.CONFLICT.value());
        errorBody.put("code", "PLAN_VERSION_CONFLICT");
        errorBody.put("message", "The planning state changed before this operation completed.");
        errorBody.put("details", new Object[] {});

        return new ResponseEntity<>(errorBody, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFailure(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        String message = "Request validation failed.";
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String detail = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            details.add(detail);
            if ("message".equals(fieldError.getField()) || message.equals("Request validation failed.")) {
                message = fieldError.getDefaultMessage();
            }
        }

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("code", "INVALID_REQUEST");
        errorBody.put("message", message);
        errorBody.put("details", details);

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("code", "MALFORMED_REQUEST");
        errorBody.put("message", "Request body is malformed or missing.");
        errorBody.put("details", new Object[] {});

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("code", status.value() + "_" + status.name());
        errorBody.put("message", ex.getReason() != null ? ex.getReason() : status.getReasonPhrase());
        errorBody.put("details", new Object[] {});

        return new ResponseEntity<>(errorBody, status);
    }
}
