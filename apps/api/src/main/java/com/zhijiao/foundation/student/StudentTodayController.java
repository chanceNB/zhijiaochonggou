package com.zhijiao.foundation.student;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import com.zhijiao.foundation.student.learning.LearningStateView;
import com.zhijiao.foundation.student.practice.TeacherAssignmentProvisioner;
import com.zhijiao.foundation.teacher.Intervention;
import com.zhijiao.foundation.teacher.InterventionAssignment;
import com.zhijiao.foundation.teacher.InterventionRepository;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/student")
public class StudentTodayController {
    private static final String DEFAULT_STUDENT_ID = "stu-xiaoming";
    private static final String DEFAULT_COURSE_ID = "course-data-structures";

    private final LearningStateEngine learningStateEngine;
    private final InterventionRepository interventionRepository;
    private final TeacherAssignmentProvisioner provisioner;
    private final Clock clock;

    public StudentTodayController(LearningStateEngine learningStateEngine,
                                   InterventionRepository interventionRepository,
                                   TeacherAssignmentProvisioner provisioner, Clock clock) {
        this.learningStateEngine = learningStateEngine;
        this.interventionRepository = interventionRepository;
        this.provisioner = provisioner;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @GetMapping("/today")
    public ApiEnvelope<TodayResponse> today(
            @RequestParam(required = false, defaultValue = DEFAULT_COURSE_ID) @NotBlank String courseId,
            @RequestParam(required = false, defaultValue = DEFAULT_STUDENT_ID) @NotBlank String studentId,
            HttpServletRequest request) {
        InterventionAssignment assignment = interventionRepository.findCurrentAssignment(studentId, courseId).orElse(null);
        if (assignment != null) provisioner.ensureProvisioned(assignment);
        LearningStateView state = learningStateEngine.read(studentId, courseId,
                assignment == null ? null : assignment.knowledgePointId());
        Intervention intervention = assignment == null ? null
                : interventionRepository.findById(assignment.interventionId()).orElse(null);
        String knowledgePointName = state.state().knowledgePointName();
        String assignmentTitle = intervention == null ? "教师布置的定向练习"
                : interventionRepository.findStrategyTitle(intervention.interventionId()).orElse("教师布置的定向练习");
        TeacherAssignmentView assignmentView = assignment == null ? null : TeacherAssignmentView.from(assignment, knowledgePointName, assignmentTitle);
        String knowledgePointId = state.state().knowledgePointId();
        NextAction nextAction = assignment == null
                ? new NextAction("AI_COACH_DIAGNOSTIC", "Continue learning", knowledgePointId, knowledgePointName, 10)
                : new NextAction("TEACHER_ASSIGNMENT", assignmentTitle, assignment.knowledgePointId(), knowledgePointName, 10);
        TodayResponse response = new TodayResponse(studentId, nextAction, assignmentView,
                LearningStateSummary.from(state), intervention == null ? null : intervention.demoCaseId());
        return ApiEnvelope.success((String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE), response,
                Instant.now(clock));
    }

    public record TodayResponse(String studentId, NextAction nextAction, TeacherAssignmentView teacherAssignment,
                                LearningStateSummary learningState, String demoCaseId) {
    }

    public record NextAction(String type, String title, String knowledgePointId, String knowledgePointName, int estimatedMinutes) {
    }

    public record TeacherAssignmentView(String assignmentId, String interventionId, String practiceSetId,
                                        String studentId, String courseId, String classId, String knowledgePointId,
                                        String status, Instant dueAt, Instant createdAt, String demoRunId,
                                        String demoCaseId, String correlationId, String sourceVersion,
                                        String knowledgePointName, String title, String source) {
            static TeacherAssignmentView from(InterventionAssignment assignment, String knowledgePointName, String title) {
                return new TeacherAssignmentView(assignment.assignmentId(), assignment.interventionId(), assignment.practiceSetId(),
                    assignment.studentId(), assignment.courseId(), assignment.classId(), assignment.knowledgePointId(),
                    assignment.status(), assignment.dueAt(), assignment.createdAt(), assignment.demoRunId(),
                    assignment.demoCaseId(), assignment.correlationId(), assignment.sourceVersion(), knowledgePointName, title,
                    "TEACHER_INTERVENTION");
        }
    }

    public record LearningStateSummary(String knowledgePointId, double mastery, double confidence,
                                       double forgettingRisk, int evidenceCount) {
        static LearningStateSummary from(LearningStateView view) {
            return new LearningStateSummary(view.state().knowledgePointId(), view.state().masteryProbability(),
                    view.state().confidence(), view.state().forgettingRisk(), view.state().evidenceCount());
        }
    }
}
