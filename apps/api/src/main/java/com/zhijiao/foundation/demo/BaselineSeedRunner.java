package com.zhijiao.foundation.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.baseline.seed-on-start", havingValue = "true")
public class BaselineSeedRunner implements ApplicationRunner {
    private final BaselineSeedService baselineSeedService;

    public BaselineSeedRunner(BaselineSeedService baselineSeedService) {
        this.baselineSeedService = baselineSeedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        baselineSeedService.seed();
    }
}
