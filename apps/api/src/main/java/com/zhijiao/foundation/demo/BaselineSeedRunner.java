package com.zhijiao.foundation.demo;

import com.zhijiao.foundation.student.learning.LearningStateEngine;
import com.zhijiao.foundation.analytics.AnalyticsProjectionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@ConditionalOnProperty(name = "app.baseline.seed-on-start", havingValue = "true")
@Order(100)
public class BaselineSeedRunner implements ApplicationRunner {
    private final BaselineSeedService baselineSeedService;
    private final LearningStateEngine learningStateEngine;
    private final AnalyticsProjectionService analyticsProjectionService;

    public BaselineSeedRunner(BaselineSeedService baselineSeedService, LearningStateEngine learningStateEngine,
                              AnalyticsProjectionService analyticsProjectionService) {
        this.baselineSeedService = baselineSeedService;
        this.learningStateEngine = learningStateEngine;
        this.analyticsProjectionService = analyticsProjectionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
        analyticsProjectionService.refresh();
    }
}
