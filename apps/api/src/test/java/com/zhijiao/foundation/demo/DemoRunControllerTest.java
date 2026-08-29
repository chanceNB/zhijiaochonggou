package com.zhijiao.foundation.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BaselineSeedService baselineSeedService;

    @BeforeEach
    void resetFixture() {
        jdbcTemplate.update("delete from app.coach_citations");
        jdbcTemplate.update("delete from app.coach_diagnostic_questions");
        jdbcTemplate.update("delete from app.coach_messages");
        jdbcTemplate.update("delete from app.coach_sessions");
        jdbcTemplate.update("delete from app.knowledge_chunks");
        jdbcTemplate.update("delete from app.knowledge_documents");
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates");
        jdbcTemplate.update("delete from app.student_learning_abilities");
        jdbcTemplate.update("delete from app.demo_runs");
        jdbcTemplate.update("delete from app.practice_attempts");
        jdbcTemplate.update("delete from app.learning_events");
        jdbcTemplate.update("delete from app.learning_snapshots");
        jdbcTemplate.update("delete from app.question_items");
        jdbcTemplate.update("delete from app.knowledge_points");
        jdbcTemplate.update("delete from app.students");
        jdbcTemplate.update("delete from app.classrooms");
        jdbcTemplate.update("delete from app.courses");
        jdbcTemplate.update("delete from app.baseline_metadata");
        baselineSeedService.seed();
    }

    @Test
    void createReturnsUnifiedDemoRunEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/demo/runs")
                        .header("X-Request-Id", "req-create")
                        .header("Idempotency-Key", "idem-create-1")
                        .contentType("application/json")
                        .content("""
                                {"demoCaseId":"DEMO-GRAPH-001","baselineVersion":"baseline-ds-v1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.requestId", equalTo("req-create")))
                .andExpect(jsonPath("$.data.demoRunId", startsWith("demo-run-")))
                .andExpect(jsonPath("$.data.studentId", equalTo("stu-xiaoming")))
                .andExpect(jsonPath("$.data.status", equalTo("ACTIVE")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void invalidBaselineReturnsValidationEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/demo/runs")
                        .header("Idempotency-Key", "idem-invalid-1")
                        .contentType("application/json")
                        .content("""
                                {"demoCaseId":"DEMO-GRAPH-001","baselineVersion":"missing-baseline"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getResetAndTraceExposeRunLifecycle() throws Exception {
        String response = mockMvc.perform(post("/api/v1/demo/runs")
                        .header("Idempotency-Key", "idem-lifecycle-create")
                        .contentType("application/json")
                        .content("""
                                {"demoCaseId":"DEMO-GRAPH-001","baselineVersion":"baseline-ds-v1"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String runId = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .findAndAddModules().build().readTree(response).path("data").path("demoRunId").asText();

        mockMvc.perform(get("/api/v1/demo/runs/{demoRunId}", runId)
                        .header("X-Request-Id", "req-get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId", equalTo("req-get")))
                .andExpect(jsonPath("$.data.demoRunId", equalTo(runId)))
                .andExpect(jsonPath("$.data.stage", equalTo("BASELINE_READY")))
                .andExpect(jsonPath("$.data.refs.studentId", equalTo("stu-xiaoming")));

        mockMvc.perform(get("/api/v1/analytics/demo-traces/DEMO-GRAPH-001")
                        .param("demoRunId", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.demoCaseId", equalTo("DEMO-GRAPH-001")))
                .andExpect(jsonPath("$.data.demoRunId", equalTo(runId)))
                .andExpect(jsonPath("$.data.events").isArray());

        mockMvc.perform(post("/api/v1/demo/runs/{demoRunId}/reset", runId)
                        .header("Idempotency-Key", "idem-lifecycle-reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newDemoRunId", not(equalTo(runId))))
                .andExpect(jsonPath("$.data.baselineVersion", equalTo("baseline-ds-v1")));
    }

    @Test
    void unknownRunReturnsNotFoundEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/demo/runs/missing-run"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void missingTraceRunQueryReturnsValidationEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/demo-traces/DEMO-GRAPH-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("VALIDATION_ERROR")));
    }
}
