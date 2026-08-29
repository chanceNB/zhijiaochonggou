package com.zhijiao.foundation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfiguration {
    @Bean
    FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIdFilter());
        registration.setOrder(0);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    FilterRegistrationBean<IdempotencyKeyFilter> idempotencyKeyFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<IdempotencyKeyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IdempotencyKeyFilter(objectMapper));
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
