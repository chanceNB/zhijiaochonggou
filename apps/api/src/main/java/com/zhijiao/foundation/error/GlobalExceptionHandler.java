package com.zhijiao.foundation.error;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiEnvelope<Void>> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, Object> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fields, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiEnvelope<Void>> unexpected(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Service temporarily unavailable", null, request);
    }

    private ResponseEntity<ApiEnvelope<Void>> response(
            HttpStatus status,
            String code,
            String message,
            Map<String, Object> details,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ResponseEntity.status(status)
                .body(ApiEnvelope.failure(code, message, requestId, details, Instant.now()));
    }
}
