package com.zhijiao.foundation.integration.smartbi;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/integrations/smartbi/assets")
public class SmartBiAssetController {
    private final SmartBiAssetService service;
    private final Clock clock;

    public SmartBiAssetController(SmartBiAssetService service, Clock clock) {
        this.service = service;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @GetMapping
    public ApiEnvelope<AssetListResponse> list(HttpServletRequest request) {
        return success(request, new AssetListResponse(service.list()));
    }

    @GetMapping("/{assetKey}")
    public ApiEnvelope<SmartBiAsset> get(@PathVariable String assetKey, HttpServletRequest request) {
        return success(request, service.get(assetKey));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        return ApiEnvelope.success((String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE), data,
                Instant.now(clock));
    }

    public record AssetListResponse(List<SmartBiAsset> assets) {
    }
}
