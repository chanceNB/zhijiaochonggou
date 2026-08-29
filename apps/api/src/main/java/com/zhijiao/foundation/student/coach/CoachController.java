package com.zhijiao.foundation.student.coach;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.knowledge.Citation;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/coach")
public class CoachController {
    private final CoachOrchestrator orchestrator;
    private final Clock clock;

    public CoachController(CoachOrchestrator orchestrator, Clock clock) {
        this.orchestrator = orchestrator;
        this.clock = clock;
    }

    @PostMapping(value = "/sessions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<CoachSessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            HttpServletRequest servletRequest) {
        CoachSession session = orchestrator.createSession(
                request.studentId(), request.courseId(), request.knowledgePointId(), request.mode(), idempotencyKey);
        return success(servletRequest, new CoachSessionResponse(session, List.of(), List.of()));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiEnvelope<CoachSessionResponse> getSession(@PathVariable String sessionId,
                                                         HttpServletRequest servletRequest) {
        CoachSessionView view = orchestrator.getSession(sessionId);
        return success(servletRequest, new CoachSessionResponse(view.session(), view.messages(), view.diagnosticQuestions()));
    }

    @PostMapping(value = "/sessions/{sessionId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<CoachMessageResponse> sendMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody MessageRequest request,
            HttpServletRequest servletRequest) {
        CoachMessageResult result = orchestrator.sendMessage(sessionId, request.message());
        return success(servletRequest, new CoachMessageResponse(result.assistantMessage(), result.citations(),
                result.actions(), result.ragStatus().name()));
    }

    @PostMapping(value = "/sessions/{sessionId}/diagnostic-sets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<DiagnosticSetResponse> generateDiagnosticSet(
            @PathVariable String sessionId,
            @Valid @RequestBody DiagnosticSetRequest request,
            HttpServletRequest servletRequest) {
        if (request.questionCount() != 2) {
            throw new IllegalArgumentException("questionCount must be exactly 2");
        }
        CoachSession session = orchestrator.getSession(sessionId).session();
        DiagnosticSetResult result = orchestrator.generateDiagnosticSet(sessionId, session.studentId(),
                session.courseId(), request.knowledgePointId());
        return success(servletRequest, new DiagnosticSetResponse(result.practiceSetId(), result.questions().size(),
                result.questions(), result.ragStatus().name()));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, data, Instant.now(clock));
    }

    public record CreateSessionRequest(@NotBlank String courseId, @NotBlank String studentId,
                                       String knowledgePointId, String mode) {
    }

    public record MessageRequest(@NotBlank String message) {
    }

    public record DiagnosticSetRequest(@NotBlank String knowledgePointId, @NotNull Integer questionCount) {
    }

    public record CoachSessionResponse(String sessionId, String studentId, String courseId, String knowledgePointId,
                                       String mode, String status, String ragStatus, CoachLearningContext context,
                                       List<CoachMessage> messages, List<DiagnosticQuestion> diagnosticQuestions) {
        CoachSessionResponse(CoachSession session, List<CoachMessage> messages, List<DiagnosticQuestion> questions) {
            this(session.sessionId(), session.studentId(), session.courseId(), session.knowledgePointId(), session.mode(),
                    session.status(), session.ragStatus().name(), new CoachLearningContext(session.mastery(),
                            session.confidence(), session.forgettingRisk(), session.weaknessScore(), session.reasonCodes(),
                            session.learningModelVersion(), session.sourceVersion()), messages, questions);
        }
    }

    public record CoachLearningContext(double mastery, double confidence, double forgettingRisk,
                                       double weaknessScore, String reasonCodes, String modelVersion,
                                       String sourceVersion) {
    }

    public record CoachMessageResponse(String assistantMessage, List<Citation> citations,
                                       List<CoachMessageResult.CoachAction> actions, String ragStatus) {
    }

    public record DiagnosticSetResponse(String practiceSetId, int questionCount,
                                        List<DiagnosticQuestion> questions, String ragStatus) {
    }
}
