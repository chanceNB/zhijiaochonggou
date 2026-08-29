package com.zhijiao.foundation.health;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.api.HealthStatus;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private final Clock clock;

    public HealthController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping
    public ApiEnvelope<HealthStatus> health(HttpServletRequest request) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, new HealthStatus("UP", "foundation-api"), Instant.now(clock));
    }
}
