package com.zhijiao.foundation.teacher;

import org.springframework.stereotype.Component;

/**
 * The verified SmartBI integration seam. The teacher copies the AIChat result
 * into this adapter until a platform callback is actually available.
 */
@Component
public class ManualCaptureAdapter implements AnalysisRecommendationPort {
    private final AnalysisRecommendationService service;

    public ManualCaptureAdapter(AnalysisRecommendationService service) {
        this.service = service;
    }

    @Override
    public AnalysisRecommendation capture(AnalysisRecommendationCapture command) {
        return service.capture(command);
    }
}
