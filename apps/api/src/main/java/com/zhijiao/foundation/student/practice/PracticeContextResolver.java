package com.zhijiao.foundation.student.practice;

import org.springframework.stereotype.Component;

@Component
public class PracticeContextResolver {
    private final PracticeRepository repository;

    public PracticeContextResolver(PracticeRepository repository) {
        this.repository = repository;
    }

    public PracticeRepository.DemoContext activeDemo(String studentId, String courseId) {
        return repository.findActiveDemo(studentId, courseId)
                .orElseThrow(() -> new DomainRuleViolationException("An active demo run is required for LIVE_DEMO practice"));
    }
}
