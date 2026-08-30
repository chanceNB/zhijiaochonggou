package com.zhijiao.foundation.error;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.demo.DemoRunNotFoundException;
import com.zhijiao.foundation.student.learning.LearningStateNotFoundException;
import com.zhijiao.foundation.knowledge.KnowledgeUnavailableException;
import com.zhijiao.foundation.knowledge.EmbeddingUnavailableException;
import com.zhijiao.foundation.student.coach.CoachSessionNotFoundException;
import com.zhijiao.foundation.student.coach.DiagnosticValidationException;
import com.zhijiao.foundation.student.coach.InvalidLlmOutputException;
import com.zhijiao.foundation.student.coach.LlmTimeoutException;
import com.zhijiao.foundation.student.coach.LlmUnavailableException;
import com.zhijiao.foundation.student.practice.DomainRuleViolationException;
import com.zhijiao.foundation.student.practice.PracticeAttemptNotFoundException;
import com.zhijiao.foundation.student.practice.PracticeSetNotFoundException;
import com.zhijiao.foundation.student.practice.WrongBookItemNotFoundException;
import com.zhijiao.foundation.analytics.AnalyticsExportNotFoundException;
import com.zhijiao.foundation.teacher.InterventionNotFoundException;
import com.zhijiao.foundation.teacher.PreconditionFailedException;
import com.zhijiao.foundation.teacher.RecommendationNotFoundException;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiEnvelope<Void>> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, Object> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fields, request);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiEnvelope<Void>> malformedRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", null, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiEnvelope<Void>> invalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), null, request);
    }

    @ExceptionHandler(DemoRunNotFoundException.class)
    ResponseEntity<ApiEnvelope<Void>> notFound(DemoRunNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), null, request);
    }

    @ExceptionHandler(LearningStateNotFoundException.class)
    ResponseEntity<ApiEnvelope<Void>> learningStateNotFound(LearningStateNotFoundException exception,
                                                              HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), null, request);
    }

    @ExceptionHandler(CoachSessionNotFoundException.class)
    ResponseEntity<ApiEnvelope<Void>> coachSessionNotFound(CoachSessionNotFoundException exception,
                                                            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", exception.getMessage(), null, request);
    }

    @ExceptionHandler({PracticeSetNotFoundException.class, PracticeAttemptNotFoundException.class,
            WrongBookItemNotFoundException.class, AnalyticsExportNotFoundException.class,
            RecommendationNotFoundException.class, InterventionNotFoundException.class})
    ResponseEntity<ApiEnvelope<Void>> practiceNotFound(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), null, request);
    }

    @ExceptionHandler(DomainRuleViolationException.class)
    ResponseEntity<ApiEnvelope<Void>> domainRuleViolation(DomainRuleViolationException exception,
                                                            HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "DOMAIN_RULE_VIOLATION", exception.getMessage(), null, request);
    }

    @ExceptionHandler(KnowledgeUnavailableException.class)
    ResponseEntity<ApiEnvelope<Void>> knowledgeUnavailable(KnowledgeUnavailableException exception,
                                                             HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "RAG_UNAVAILABLE", exception.getMessage(), null, request);
    }

    @ExceptionHandler(EmbeddingUnavailableException.class)
    ResponseEntity<ApiEnvelope<Void>> embeddingUnavailable(EmbeddingUnavailableException exception,
                                                             HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "RAG_UNAVAILABLE", exception.getMessage(), null, request);
    }

    @ExceptionHandler(LlmTimeoutException.class)
    ResponseEntity<ApiEnvelope<Void>> llmTimeout(LlmTimeoutException exception, HttpServletRequest request) {
        return response(HttpStatus.GATEWAY_TIMEOUT, "UPSTREAM_TIMEOUT", exception.getMessage(), null, request);
    }

    @ExceptionHandler(LlmUnavailableException.class)
    ResponseEntity<ApiEnvelope<Void>> llmUnavailable(LlmUnavailableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_GATEWAY, "AI_UPSTREAM_ERROR", exception.getMessage(), null, request);
    }

    @ExceptionHandler({DiagnosticValidationException.class, InvalidLlmOutputException.class})
    ResponseEntity<ApiEnvelope<Void>> invalidLlmOutput(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_GATEWAY, "LLM_OUTPUT_INVALID", exception.getMessage(), null, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiEnvelope<Void>> stateConflict(IllegalStateException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "STATE_CONFLICT", exception.getMessage(), null, request);
    }

    @ExceptionHandler(PreconditionFailedException.class)
    ResponseEntity<ApiEnvelope<Void>> preconditionFailed(PreconditionFailedException exception,
                                                          HttpServletRequest request) {
        return response(HttpStatus.PRECONDITION_FAILED, "PRECONDITION_FAILED", exception.getMessage(), null, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiEnvelope<Void>> unexpected(Exception exception, HttpServletRequest request) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        LOGGER.error("Unhandled exception requestId={} uri={} exceptionClass={} exceptionMessage={}",
                requestId, request.getRequestURI(), exception.getClass().getName(), exception.getMessage(), exception);
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
