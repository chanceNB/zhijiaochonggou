package com.zhijiao.foundation.student.practice;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

@RestController
@RequestMapping("/api/v1/student")
public class WrongBookController {
    private static final String DEFAULT_STUDENT_ID = "stu-xiaoming";
    private final WrongBookService wrongBookService;
    private final Clock clock;

    public WrongBookController(WrongBookService wrongBookService, Clock clock) {
        this.wrongBookService = wrongBookService;
        this.clock = clock;
    }

    @PostMapping(value = "/practice-attempts/{attemptId}/wrong-book", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<WrongBookItem> add(@PathVariable String attemptId, @RequestParam(defaultValue = DEFAULT_STUDENT_ID) String studentId,
                                          @RequestBody(required = false) AddWrongBookRequest body, HttpServletRequest request) {
        return success(request, wrongBookService.add(studentId, attemptId, body == null ? null : body.reason()));
    }

    @GetMapping("/wrong-book")
    public ApiEnvelope<WrongBookPage> list(@RequestParam(defaultValue = DEFAULT_STUDENT_ID) String studentId,
                                           @RequestParam(required = false) String knowledgePointId,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           HttpServletRequest request) {
        return success(request, wrongBookService.list(studentId, knowledgePointId, status, page, size));
    }

    @PostMapping(value = "/wrong-book/{wrongItemId}/review", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<WrongBookService.WrongBookReviewResult> review(@PathVariable String wrongItemId,
                                                                       @RequestParam(defaultValue = DEFAULT_STUDENT_ID) String studentId,
                                                                       @Valid @RequestBody ReviewRequest body,
                                                                       @RequestHeader("Idempotency-Key") String ignored,
                                                                       HttpServletRequest request) {
        String answer = body.answer() == null ? null : body.answer().isTextual() ? body.answer().asText() : body.answer().toString();
        return success(request, wrongBookService.review(studentId, wrongItemId, answer, body.durationSeconds()));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, data, Instant.now(clock));
    }

    public record AddWrongBookRequest(String reason) {
    }

    public record ReviewRequest(@NotNull JsonNode answer, @Positive int durationSeconds) {
    }
}
