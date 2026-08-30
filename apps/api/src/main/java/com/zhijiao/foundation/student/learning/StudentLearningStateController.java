package com.zhijiao.foundation.student.learning;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/student")
public class StudentLearningStateController {
    private static final String DEFAULT_DEMO_STUDENT_ID = "stu-xiaoming";

    private final LearningStateEngine learningStateEngine;
    private final GrowthReadModelService growthReadModelService;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public StudentLearningStateController(LearningStateEngine learningStateEngine, GrowthReadModelService growthReadModelService, Clock clock) {
        this.learningStateEngine = learningStateEngine;
        this.growthReadModelService = growthReadModelService;
        this.clock = clock;
    }

    public StudentLearningStateController(LearningStateEngine learningStateEngine, Clock clock) {
        this(learningStateEngine, null, clock);
    }

    @GetMapping("/learning-state")
    public ApiEnvelope<LearningStateResponse> getLearningState(
            @RequestParam @NotBlank String courseId,
            @RequestParam(required = false) String knowledgePointId,
            @RequestParam(required = false, defaultValue = DEFAULT_DEMO_STUDENT_ID) String studentId,
            HttpServletRequest request) {
        LearningStateView view = learningStateEngine.read(studentId, courseId, knowledgePointId);
        StudentKnowledgeState state = view.state();
        StudentAbilityState ability = view.ability();
        LearningStateResponse response = new LearningStateResponse(
                state.studentId(), state.courseId(), state.knowledgePointId(), state.knowledgePointName(),
                state.masteryProbability(), state.confidence(), state.forgettingRisk(), state.evidenceCount(),
                state.lastEvidenceAt(), ability.theta(), ability.thetaUncertainty(), state.masteryModelVersion(),
                state.abilityModelVersion(), state.forgettingModelVersion(), state.confidenceModelVersion(),
                List.copyOf(view.weakKnowledgePoints().stream().map(StudentLearningStateController::toCandidate).toList()));
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, response, Instant.now(clock));
    }

    @GetMapping("/growth")
    public ApiEnvelope<GrowthReadModel> getGrowth(
            @RequestParam @NotBlank String courseId,
            @RequestParam(required = false, defaultValue = DEFAULT_DEMO_STUDENT_ID) String studentId,
            HttpServletRequest request) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        GrowthReadModel growth = growthReadModelService == null
                ? learningStateEngine.readGrowth(studentId, courseId)
                : growthReadModelService.read(studentId, courseId);
        return ApiEnvelope.success(requestId, growth, Instant.now(clock));
    }

    private static WeakKnowledgePointResponse toCandidate(WeakKnowledgePointCandidate candidate) {
        return new WeakKnowledgePointResponse(candidate.knowledgePointId(), candidate.knowledgePointName(),
                candidate.weaknessScore(), candidate.confidence(), candidate.evidenceCount(),
                candidate.rankPosition(), candidate.reasonCodes());
    }

    public record LearningStateResponse(
            String studentId,
            String courseId,
            String knowledgePointId,
            String knowledgePointName,
            double mastery,
            double confidence,
            double forgettingRisk,
            int evidenceCount,
            Instant lastEvidenceAt,
            double theta,
            double thetaUncertainty,
            String masteryModelVersion,
            String abilityModelVersion,
            String forgettingModelVersion,
            String confidenceModelVersion,
            List<WeakKnowledgePointResponse> weakKnowledgePoints
    ) {
    }

    public record WeakKnowledgePointResponse(
            String knowledgePointId,
            String name,
            double weaknessScore,
            double confidence,
            int evidenceCount,
            int rank,
            List<String> reasonCodes
    ) {
    }
}
