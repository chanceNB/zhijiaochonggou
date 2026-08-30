package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherInterventionController {
    private final AnalysisRecommendationPort recommendationPort;
    private final AnalysisRecommendationService recommendationService;
    private final InterventionService interventionService;
    private final InterventionOutcomeService interventionOutcomeService;
    private final Clock clock;

    public TeacherInterventionController(AnalysisRecommendationPort recommendationPort,
                                         AnalysisRecommendationService recommendationService,
                                         InterventionService interventionService,
                                         InterventionOutcomeService interventionOutcomeService, Clock clock) {
        this.recommendationPort = recommendationPort;
        this.recommendationService = recommendationService;
        this.interventionService = interventionService;
        this.interventionOutcomeService = interventionOutcomeService;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @PostMapping(value = "/analysis-recommendations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<CaptureResponse> capture(@Valid @RequestBody CaptureRequest body,
                                                 @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                 HttpServletRequest request) {
        AnalysisRecommendation recommendation = recommendationPort.capture(new AnalysisRecommendationCapture(
                body.studentId(), body.courseId(), body.classId(), body.knowledgePointId(), body.demoRunId(),
                body.demoCaseId(), body.correlationId(), body.analysisSummary(), body.evidenceRefs(), body.candidates().stream()
                .map(candidate -> new AnalysisRecommendationCapture.Candidate(candidate.strategyCode(), candidate.title(),
                        candidate.rationale(), candidate.actionDescription())).toList(), body.source(), body.generatedAt(),
                idempotencyKey));
        return success(request, new CaptureResponse(recommendation.recommendationId(), recommendation.status()));
    }

    @GetMapping("/analysis-recommendations/{recommendationId}")
    public ApiEnvelope<RecommendationResponse> getRecommendation(@PathVariable String recommendationId,
                                                                 HttpServletRequest request) {
        return success(request, RecommendationResponse.from(recommendationService.get(recommendationId)));
    }

    @PostMapping(value = "/interventions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<InterventionResponse> propose(@Valid @RequestBody ProposeInterventionRequest body,
                                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     HttpServletRequest request) {
        return success(request, response(interventionService.propose(body.recommendationId(), body.strategyCode(),
                body.teacherRationale(), idempotencyKey)));
    }

    @PostMapping("/interventions/{interventionId}/approve")
    public ApiEnvelope<InterventionResponse> approve(@PathVariable String interventionId,
                                                     @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     HttpServletRequest request) {
        return success(request, response(interventionService.approve(interventionId, ifMatch, idempotencyKey)));
    }

    @PostMapping("/interventions/{interventionId}/commit")
    public ApiEnvelope<InterventionResponse> commit(@PathVariable String interventionId,
                                                     @RequestBody(required = false) CommitRequest body,
                                                     @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     HttpServletRequest request) {
        Instant dueAt = body == null ? null : body.dueAt();
        return success(request, response(interventionService.commit(interventionId, ifMatch, idempotencyKey, dueAt)));
    }

    @GetMapping("/interventions/{interventionId}")
    public ApiEnvelope<InterventionResponse> getIntervention(@PathVariable String interventionId,
                                                             HttpServletRequest request) {
        return success(request, response(interventionService.get(interventionId)));
    }

    @GetMapping("/interventions/{interventionId}/outcome")
    public ApiEnvelope<InterventionOutcome> getOutcome(@PathVariable String interventionId,
                                                       HttpServletRequest request) {
        return success(request, interventionOutcomeService.getByIntervention(interventionId));
    }

    private InterventionResponse response(Intervention intervention) {
        InterventionAssignment assignment = interventionService.assignment(intervention.interventionId());
        AssignmentResponse assignmentResponse = assignment == null ? null : AssignmentResponse.from(assignment);
        return new InterventionResponse(intervention.interventionId(), intervention.recommendationId(),
                intervention.strategyCode(), intervention.predictedLift(),
                new PredictionInterval(intervention.predictionLow(), intervention.predictionHigh()), intervention.status(),
                intervention.version(), intervention.assignmentId(), assignment == null ? null : assignment.practiceSetId(),
                assignmentResponse);
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        return ApiEnvelope.success((String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE), data,
                Instant.now(clock));
    }

    public record CaptureRequest(
            @NotBlank String studentId,
            @NotBlank String courseId,
            String classId,
            @NotBlank String knowledgePointId,
            String demoRunId,
            String demoCaseId,
            String correlationId,
            @NotBlank String analysisSummary,
            List<String> evidenceRefs,
            @Size(min = 3, max = 3) List<@Valid CandidateRequest> candidates,
            @NotBlank String source,
            Instant generatedAt
    ) {
    }

    public record CandidateRequest(@NotBlank String strategyCode, @NotBlank String title,
                                   @NotBlank String rationale, @NotBlank String actionDescription) {
    }

    public record CaptureResponse(String recommendationId, String status) {
    }

    public record RecommendationResponse(String recommendationId, String studentId, String courseId, String classId,
                                         String knowledgePointId, String demoRunId, String demoCaseId,
                                         String correlationId, String analysisSummary, List<String> evidenceRefs,
                                         List<CandidateResponse> candidates,
                                         String source, String captureMode, String status, Instant generatedAt,
                                         Instant capturedAt, String sourceVersion) {
        static RecommendationResponse from(AnalysisRecommendation recommendation) {
            return new RecommendationResponse(recommendation.recommendationId(), recommendation.studentId(),
                    recommendation.courseId(), recommendation.classId(), recommendation.knowledgePointId(),
                    recommendation.demoRunId(), recommendation.demoCaseId(), recommendation.correlationId(),
                    recommendation.analysisSummary(), recommendation.evidenceRefs(),
                    recommendation.candidates().stream().map(CandidateResponse::from).toList(),
                    recommendation.source(), recommendation.captureMode(), recommendation.status(), recommendation.generatedAt(),
                    recommendation.capturedAt(), recommendation.sourceVersion());
        }
    }

    public record CandidateResponse(int candidateIndex, String strategyCode, String title, String rationale,
                                     String actionDescription, String sourceSnapshot) {
        static CandidateResponse from(AnalysisRecommendation.Candidate candidate) {
            return new CandidateResponse(candidate.candidateIndex(), candidate.strategyCode(), candidate.title(),
                    candidate.rationale(), candidate.actionDescription(), candidate.sourceSnapshot());
        }
    }

    public record ProposeInterventionRequest(@NotBlank String recommendationId, @NotBlank String strategyCode,
                                             @NotBlank String teacherRationale) {
    }

    public record CommitRequest(Instant dueAt) {
    }

    public record PredictionInterval(double low, double high) {
    }

    public record InterventionResponse(String interventionId, String recommendationId, String strategyCode,
                                       double predictedLift, PredictionInterval predictionInterval, String status,
                                       int version, String assignmentId, String practiceSetId,
                                       AssignmentResponse assignment) {
    }

    public record AssignmentResponse(String assignmentId, String interventionId, String practiceSetId,
                                     String studentId, String courseId, String classId, String knowledgePointId,
                                     String status, Instant dueAt, Instant createdAt, String demoRunId,
                                     String demoCaseId, String correlationId, String sourceVersion) {
        static AssignmentResponse from(InterventionAssignment assignment) {
            return new AssignmentResponse(assignment.assignmentId(), assignment.interventionId(), assignment.practiceSetId(),
                    assignment.studentId(), assignment.courseId(), assignment.classId(), assignment.knowledgePointId(),
                    assignment.status(), assignment.dueAt(), assignment.createdAt(), assignment.demoRunId(),
                    assignment.demoCaseId(), assignment.correlationId(), assignment.sourceVersion());
        }
    }
}
