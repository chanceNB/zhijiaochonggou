package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherReadModelController {
    private final TeacherReadModelService service;
    private final Clock clock;

    public TeacherReadModelController(TeacherReadModelService service, Clock clock) {
        this.service = service;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @GetMapping("/workbench")
    public ApiEnvelope<TeacherReadModelService.WorkbenchResponse> workbench(HttpServletRequest request) {
        return success(request, service.workbench());
    }

    @GetMapping("/students/{studentId}/profile")
    public ApiEnvelope<TeacherReadModelService.ProfileResponse> profile(@PathVariable String studentId,
                                                                          @RequestParam String courseId,
                                                                          HttpServletRequest request) {
        return success(request, service.profile(studentId, courseId));
    }

    @GetMapping("/diagnosis-cases/{caseId}")
    public ApiEnvelope<TeacherReadModelService.DiagnosisResponse> diagnosis(@PathVariable String caseId,
                                                                              HttpServletRequest request) {
        return success(request, service.diagnosis(caseId));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        return ApiEnvelope.success((String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE), data,
                Instant.now(clock));
    }
}
