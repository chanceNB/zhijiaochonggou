package com.zhijiao.foundation.integration.smartbi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "smartbi")
public class SmartBiAssetProperties {
    private final Map<String, AssetConfig> assets = new LinkedHashMap<>();

    public Map<String, AssetConfig> getAssets() {
        return assets;
    }

    public static class AssetConfig {
        private String type = "DASHBOARD";
        private String displayName;
        private String status = "PLATFORM_PENDING";
        private String launchMode = "UNVERIFIED";
        private String resourceUrl;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLaunchMode() {
            return launchMode;
        }

        public void setLaunchMode(String launchMode) {
            this.launchMode = launchMode;
        }

        public String getResourceUrl() {
            return resourceUrl;
        }

        public void setResourceUrl(String resourceUrl) {
            this.resourceUrl = resourceUrl;
        }
    }
}
