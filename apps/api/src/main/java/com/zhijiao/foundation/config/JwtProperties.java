package com.zhijiao.foundation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(boolean enabled, String issuer) {
}
