package com.zhijiao.foundation.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/smartbi")
public class AnalyticsController {
    private final AnalyticsProjectionService projectionService;
    private final AnalyticsExportService exportService;
    private final Clock clock;

    public AnalyticsController(AnalyticsProjectionService projectionService, AnalyticsExportService exportService, Clock clock) {
        this.projectionService = projectionService;
        this.exportService = exportService;
        this.clock = clock;
    }

    @GetMapping("/datasets")
    public ApiEnvelope<DatasetCatalogResponse> datasets(HttpServletRequest request) {
        return success(request, new DatasetCatalogResponse("smartbi-exchange-v2", projectionService.catalog()));
    }

    @GetMapping("/freshness")
    public ApiEnvelope<FreshnessResponse> freshness(HttpServletRequest request) {
        List<AnalyticsFreshness> items = projectionService.freshness();
        Instant latestSource = items.stream().map(AnalyticsFreshness::latestSourceEventTime)
                .filter(java.util.Objects::nonNull).max(Instant::compareTo).orElse(null);
        Instant latestProjection = items.stream().map(AnalyticsFreshness::latestProjectionTime)
                .max(Instant::compareTo).orElse(null);
        long lagSeconds = latestSource == null || latestProjection == null ? 0 :
                Math.max(0, java.time.Duration.between(latestProjection, latestSource).getSeconds());
        String status = latestSource == null ? "NO_DATA" : lagSeconds <= 60 ? "FRESH" : "STALE";
        return success(request, new FreshnessResponse(items, latestSource, latestProjection, lagSeconds, status));
    }

    @PostMapping("/exports")
    public ApiEnvelope<AnalyticsExport> createExport(@RequestBody(required = false) ExportRequest body,
                                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     HttpServletRequest request) {
        ExportRequest input = body == null ? new ExportRequest("ACTIVE_DEMO", null) : body;
        return success(request, exportService.create(input.scope(), input.demoRunId(), idempotencyKey));
    }

    @GetMapping("/exports/{exportId}")
    public ApiEnvelope<AnalyticsExport> getExport(@PathVariable String exportId, HttpServletRequest request) {
        return success(request, exportService.get(exportId));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        return ApiEnvelope.success((String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE), data,
                Instant.now(clock));
    }

    public record DatasetCatalogResponse(String contractVersion, List<AnalyticsDataset> datasets) {
    }

    public record FreshnessResponse(List<AnalyticsFreshness> items, Instant lastBusinessEventAt,
                                    Instant lastProjectedAt, long lagSeconds, String status) {
    }

    public record ExportRequest(String scope, String demoRunId) {
        @JsonCreator
        public ExportRequest {
        }
    }
}
