package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.demo.BaselineRepository;
import com.zhijiao.foundation.demo.DemoRun;
import com.zhijiao.foundation.demo.DemoRunService;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherReadModelIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired BaselineRepository baselineRepository;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired DemoRunService demoRunService;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void ensureBaseline() {
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String questionId = "baseline-q-kp-graph-basics-06";
        baselineRepository.insertPracticeAttempt("live-teacher-" + UUID.randomUUID().toString().replace("-", ""),
                Instant.parse("2026-08-31T04:00:00Z"), BaselineSeedService.XIAOMING_ID, BaselineSeedService.COURSE_ID,
                BaselineSeedService.CLASS_ONE_ID, "kp-graph-basics", questionId, "LIVE_DIAGNOSTIC", "MEDIUM", false,
                30, 30000, 100, null, "LIVE_DEMO", "teacher-test-v1", BaselineSeedService.BASELINE_VERSION,
                run.demoRunId(), run.demoCaseId(), run.correlationId(), Instant.parse("2026-08-31T04:00:00Z"));
        learningStateEngine.recomputeForStudentCourse(BaselineSeedService.XIAOMING_ID, BaselineSeedService.COURSE_ID,
                Instant.parse("2026-08-31T04:00:00Z"), run.demoRunId(), run.demoCaseId(), run.correlationId());
    }

    @Test
    void workbenchResolvesCurrentStudentWithoutCohortDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/workbench"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.data.currentStudent.displayName", equalTo("小明")))
                .andExpect(jsonPath("$.data.currentStudent.courseName", equalTo("数据结构")))
                .andExpect(jsonPath("$.data.currentStudent.demoRunId").isString())
                .andExpect(jsonPath("$.data.currentStudent.demoCaseId", equalTo("DEMO-GRAPH-001")))
                .andExpect(jsonPath("$.data.priorityItems", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.classCount").doesNotExist());
    }

    @Test
    void profileReturnsRealAttemptsAndUnknownStudentIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/students/stu-xiaoming/profile")
                        .param("courseId", "course-data-structures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.student.displayName", equalTo("小明")))
                .andExpect(jsonPath("$.data.student.demoRunId").isString())
                .andExpect(jsonPath("$.data.recentAttempts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.learningState.knowledgePointName").isString());

        mockMvc.perform(get("/api/v1/teacher/students/missing-student/profile")
                        .param("courseId", "course-data-structures"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("RESOURCE_NOT_FOUND")));
    }

    @Test
    void diagnosisReturnsEvidenceForActiveCaseAndUnknownCaseIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/diagnosis-cases/DEMO-GRAPH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caseId", equalTo("DEMO-GRAPH-001")))
                .andExpect(jsonPath("$.data.primaryHypothesis").isString())
                .andExpect(jsonPath("$.data.evidence", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.evidence[2]", not(containsString("T04:00:00"))))
                .andExpect(jsonPath("$.data.evidence[2]", containsString("8月31日 12:00")))
                .andExpect(jsonPath("$.data.confidence").isNumber());

        mockMvc.perform(get("/api/v1/teacher/diagnosis-cases/missing-case"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("RESOURCE_NOT_FOUND")));
    }

    @Test
    void excludesRecommendationAndInterventionFromPreviousDemoRun() throws Exception {
        DemoRun previous = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        Thread.sleep(5);
        DemoRun current = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String recommendationId = "stale-recommendation-" + UUID.randomUUID().toString().replace("-", "");
        Instant capturedAt = Instant.parse("2026-08-31T03:00:00Z");
        jdbcTemplate.update("""
                insert into app.analysis_recommendations
                    (recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                     demo_run_id, demo_case_id, correlation_id, analysis_summary, evidence_refs,
                     source, capture_mode, status, generated_at, captured_at, source_version, idempotency_key)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SMARTBI_AICHAT', 'MANUAL',
                        'PENDING_TEACHER_REVIEW', ?, ?, ?, ?)
                """, recommendationId, BaselineSeedService.XIAOMING_ID, BaselineSeedService.COURSE_ID,
                BaselineSeedService.CLASS_ONE_ID, BaselineSeedService.BFS_DFS_ID, previous.demoRunId(),
                previous.demoCaseId(), previous.correlationId(), "旧运行推荐", "[]", capturedAt, capturedAt,
                "teacher-test-v1", "stale-key-" + UUID.randomUUID());
        String interventionId = "stale-intervention-" + UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("""
                insert into app.interventions
                    (intervention_id, recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                     strategy_code, teacher_rationale, predicted_lift, prediction_low, prediction_high,
                     status, version, assignment_id, demo_run_id, demo_case_id, correlation_id,
                     source_version, idempotency_key, created_at)
                values (?, ?, ?, ?, ?, ?, 'VISUAL_TRANSFER_PRACTICE', '旧运行干预',
                        0.2, 0.1, 0.3, 'COMMITTED', 1, null, ?, ?, ?, ?, ?, ?)
                """, interventionId, recommendationId, BaselineSeedService.XIAOMING_ID,
                BaselineSeedService.COURSE_ID, BaselineSeedService.CLASS_ONE_ID, BaselineSeedService.BFS_DFS_ID,
                previous.demoRunId(), previous.demoCaseId(), previous.correlationId(), "teacher-test-v1",
                "stale-intervention-key-" + UUID.randomUUID(), capturedAt);

        mockMvc.perform(get("/api/v1/teacher/workbench"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStudent.demoRunId", equalTo(current.demoRunId())))
                .andExpect(jsonPath("$.data.pendingRecommendations", hasSize(0)))
                .andExpect(jsonPath("$.data.pendingOutcomes", hasSize(0)));
        mockMvc.perform(get("/api/v1/teacher/students/stu-xiaoming/profile")
                        .param("courseId", "course-data-structures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.student.demoRunId", equalTo(current.demoRunId())))
                .andExpect(jsonPath("$.data.intervention").doesNotExist());
    }
}
