package com.zhijiao.foundation.demo;

import com.zhijiao.foundation.student.learning.LearningStateEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.baseline.seed-on-start", havingValue = "true")
public class BaselineSeedRunner implements ApplicationRunner {
    private final BaselineSeedService baselineSeedService;
    private final LearningStateEngine learningStateEngine;

    public BaselineSeedRunner(BaselineSeedService baselineSeedService, LearningStateEngine learningStateEngine) {
        this.baselineSeedService = baselineSeedService;
        this.learningStateEngine = learningStateEngine;
    }

    @Override
    public void run(ApplicationArguments args) {
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
    }
}
