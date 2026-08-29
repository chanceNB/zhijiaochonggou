package com.zhijiao.foundation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.api.ApiEnvelope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/** Foundation guard for future mutating API commands. Storage/replay is owned by later tasks. */
public class IdempotencyKeyFilter extends OncePerRequestFilter {
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private final ObjectMapper objectMapper;

    public IdempotencyKeyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        boolean apiWrite = request.getRequestURI().startsWith("/api/v1/")
                && switch (request.getMethod()) {
                    case "POST", "PUT", "PATCH", "DELETE" -> true;
                    default -> false;
                };
        if (apiWrite && (request.getHeader(IDEMPOTENCY_KEY_HEADER) == null
                || request.getHeader(IDEMPOTENCY_KEY_HEADER).isBlank())) {
            String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiEnvelope.failure("VALIDATION_ERROR", "Idempotency-Key is required", requestId, null, Instant.now())
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
