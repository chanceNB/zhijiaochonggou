package com.zhijiao.foundation.demo;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DemoRunController {
    private final DemoRunService demoRunService;
    private final Clock clock;

    public DemoRunController(DemoRunService demoRunService, Clock clock) {
        this.demoRunService = demoRunService;
        this.clock = clock;
    }

    @PostMapping(value = "/demo/runs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<CreateDemoRunResponse> create(
            @Valid @RequestBody CreateDemoRunRequest request,
            HttpServletRequest servletRequest) {
        DemoRun run = demoRunService.create(request.demoCaseId(), request.baselineVersion());
        return success(servletRequest, new CreateDemoRunResponse(run.demoRunId(), run.studentId(), run.status()));
    }

    @GetMapping("/demo/runs/{demoRunId}")
    public ApiEnvelope<DemoRunResponse> get(
            @PathVariable String demoRunId,
            HttpServletRequest servletRequest) {
        DemoRun run = demoRunService.traceRun(demoRunId);
        Map<String, String> refs = Map.of(
                "studentId", run.studentId(),
                "courseId", run.courseId(),
                "classId", run.classId());
        DemoRunResponse data = new DemoRunResponse(run.demoRunId(), run.demoCaseId(), run.baselineVersion(),
                run.status(), run.stage(), refs, run.correlationId());
        return success(servletRequest, data);
    }

    @PostMapping("/demo/runs/{demoRunId}/reset")
    public ApiEnvelope<ResetDemoRunResponse> reset(
            @PathVariable String demoRunId,
            HttpServletRequest servletRequest) {
        DemoRun run = demoRunService.reset(demoRunId);
        return success(servletRequest, new ResetDemoRunResponse(run.demoRunId(), run.baselineVersion()));
    }

    @GetMapping("/analytics/demo-traces/{demoCaseId}")
    public ApiEnvelope<DemoRunTrace> trace(
            @PathVariable String demoCaseId,
            @RequestParam String demoRunId,
            HttpServletRequest servletRequest) {
        DemoRunTrace trace = demoRunService.trace(demoRunId);
        if (!demoCaseId.equals(trace.demoCaseId())) {
            throw new IllegalArgumentException("Demo case does not match demo run");
        }
        return success(servletRequest, trace);
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, data, Instant.now(clock));
    }

    public record CreateDemoRunRequest(
            @NotBlank String demoCaseId,
            @NotBlank String baselineVersion) {
    }

    public record CreateDemoRunResponse(String demoRunId, String studentId, String status) {
    }

    public record DemoRunResponse(
            String demoRunId,
            String demoCaseId,
            String baselineVersion,
            String status,
            String stage,
            Map<String, String> refs,
            String correlationId) {
    }

    public record ResetDemoRunResponse(String newDemoRunId, String baselineVersion) {
    }
}
