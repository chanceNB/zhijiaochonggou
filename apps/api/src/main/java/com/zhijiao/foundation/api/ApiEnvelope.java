package com.zhijiao.foundation.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiEnvelope<T>(
        String code,
        String message,
        String requestId,
        T data,
        Map<String, Object> details,
        Instant timestamp
) {
    public static <T> ApiEnvelope<T> success(String requestId, T data, Instant timestamp) {
        return new ApiEnvelope<>("OK", "success", requestId, data, null, timestamp);
    }

    public static <T> ApiEnvelope<T> success(String requestId, String message, T data, Instant timestamp) {
        return new ApiEnvelope<>("OK", message, requestId, data, null, timestamp);
    }

    public static <T> ApiEnvelope<T> failure(
            String code,
            String message,
            String requestId,
            Map<String, Object> details,
            Instant timestamp
    ) {
        return new ApiEnvelope<>(code, message, requestId, null, details, timestamp);
    }
}
