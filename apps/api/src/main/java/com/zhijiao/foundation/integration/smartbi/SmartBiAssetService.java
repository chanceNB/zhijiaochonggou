package com.zhijiao.foundation.integration.smartbi;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SmartBiAssetService {
    private static final Map<String, DefaultAsset> CONTRACT_ASSETS = Map.of(
            "student-risk", new DefaultAsset("DASHBOARD", "学生风险分析"),
            "intervention-outcome", new DefaultAsset("DASHBOARD", "干预成效")
    );

    private final SmartBiAssetProperties properties;

    public SmartBiAssetService(SmartBiAssetProperties properties) {
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<SmartBiAsset> list() {
        Map<String, SmartBiAsset> assets = new LinkedHashMap<>();
        CONTRACT_ASSETS.keySet().forEach(key -> assets.put(key, resolve(key)));
        properties.getAssets().keySet().stream()
                .filter(key -> !assets.containsKey(key))
                .sorted()
                .forEach(key -> assets.put(key, resolve(key)));
        return new ArrayList<>(assets.values());
    }

    @Transactional(readOnly = true)
    public SmartBiAsset get(String assetKey) {
        if (assetKey == null || assetKey.isBlank()
                || (!CONTRACT_ASSETS.containsKey(assetKey) && !properties.getAssets().containsKey(assetKey))) {
            throw new SmartBiAssetNotFoundException(assetKey);
        }
        return resolve(assetKey);
    }

    private SmartBiAsset resolve(String key) {
        DefaultAsset defaultAsset = CONTRACT_ASSETS.get(key);
        SmartBiAssetProperties.AssetConfig config = properties.getAssets().get(key);
        String type = value(config == null ? null : config.getType(), defaultAsset == null ? "DASHBOARD" : defaultAsset.type());
        String displayName = value(config == null ? null : config.getDisplayName(), defaultAsset == null ? key : defaultAsset.displayName());
        String resourceUrl = blankToNull(config == null ? null : config.getResourceUrl());
        String configuredStatus = upper(value(config == null ? null : config.getStatus(), "PLATFORM_PENDING"));
        String configuredLaunchMode = upper(value(config == null ? null : config.getLaunchMode(), "UNVERIFIED"));
        if (resourceUrl == null || !"VERIFIED".equals(configuredStatus)
                || !("IFRAME".equals(configuredLaunchMode) || "NEW_TAB".equals(configuredLaunchMode))) {
            return new SmartBiAsset(key, type, displayName, "PLATFORM_PENDING", "UNVERIFIED", null);
        }
        return new SmartBiAsset(key, type, displayName, "VERIFIED", configuredLaunchMode, resourceUrl);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String upper(String value) {
        return value.toUpperCase(Locale.ROOT);
    }

    private record DefaultAsset(String type, String displayName) {
    }
}
