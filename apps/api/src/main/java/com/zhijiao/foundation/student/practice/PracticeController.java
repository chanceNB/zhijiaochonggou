package com.zhijiao.foundation.student.practice;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@RequestMapping("/api/v1/student")
public class PracticeController {
    private final PracticeService practiceService;
    private final Clock clock;

    public PracticeController(PracticeService practiceService, Clock clock) {
        this.practiceService = practiceService;
        this.clock = clock;
    }

    @GetMapping("/practice-sets/{practiceSetId}")
    public ApiEnvelope<PracticeSetResponse> get(@PathVariable String practiceSetId, HttpServletRequest request) {
        return success(request, PracticeSetResponse.from(practiceService.getPracticeSet(practiceSetId)));
    }

    @PostMapping(value = "/practice-sets/{practiceSetId}/attempts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<PracticeAttemptResponse> submit(@PathVariable String practiceSetId,
                                                       @Valid @RequestBody SubmitAttemptRequest body,
                                                       @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                       HttpServletRequest request) {
        String answer = body.answer() == null ? null : body.answer().isTextual() ? body.answer().asText() : body.answer().toString();
        PracticeAttemptResult result = practiceService.submit(practiceSetId, body.questionId(), answer,
                body.durationSeconds(), idempotencyKey);
        return success(request, new PracticeAttemptResponse(result.attemptId(), result.correct(), result.correctAnswer(),
                result.explanation(), result.misconceptionCode(), result.canAddWrongBook(), result.canGenerateSimilar()));
    }

    @PostMapping("/practice-sets/{practiceSetId}/complete")
    public ApiEnvelope<PracticeOutcome> complete(@PathVariable String practiceSetId, HttpServletRequest request) {
        return success(request, practiceService.complete(practiceSetId));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, data, Instant.now(clock));
    }

    public record SubmitAttemptRequest(@NotBlank String questionId, @NotNull JsonNode answer,
                                       @Positive int durationSeconds) {
    }

    public record PracticeAttemptResponse(String attemptId, boolean correct, String correctAnswer, String explanation,
                                          String misconceptionCode, boolean canAddWrongBook, boolean canGenerateSimilar) {
    }

    public record PracticeSetResponse(String practiceSetId, String studentId, String courseId, String source,
                                      String status, String coachSessionId, String demoCaseId,
                                      List<StudentQuestion> questions, List<PracticeAttemptSummary> attempts) {
        static PracticeSetResponse from(PracticeSetView view) {
            PracticeSet set = view.practiceSet();
            return new PracticeSetResponse(set.practiceSetId(), set.studentId(), set.courseId(), set.source(), set.status(),
                    set.coachSessionId(), set.demoCaseId(), view.questions(), view.attempts());
        }
    }
}
