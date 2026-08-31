package com.zhijiao.foundation.integration.smartbi;

public class SmartBiAssetNotFoundException extends RuntimeException {
    public SmartBiAssetNotFoundException(String assetKey) {
        super("SmartBI asset not found: " + assetKey);
    }
}
