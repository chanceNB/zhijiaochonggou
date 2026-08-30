package com.zhijiao.foundation.teacher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zhijiao.foundation.demo.BaselineSeedService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherInterventionIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired DemoRunService demoRunService;

    @BeforeEach
    void resetTeacherFacts() {
        jdbcTemplate.update("delete from app.intervention_assignments");
        jdbcTemplate.update("delete from app.interventions");
        jdbcTemplate.update("delete from app.analysis_recommendation_candidates");
        jdbcTemplate.update("delete from app.analysis_recommendations");
        jdbcTemplate.update("delete from app.practice_questions where practice_set_id in (select practice_set_id from app.practice_sets where source = 'TEACHER_ASSIGNMENT')");
        jdbcTemplate.update("delete from app.practice_sets where source = 'TEACHER_ASSIGNMENT'");
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
    }

    @Test
    void capturesThreeImmutableCandidatesAndCommitsOneAssignment() throws Exception {
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String captureKey = "t07-capture-1";
        String response = mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", captureKey)
                        .contentType("application/json")
                        .content(captureBody(run)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("PENDING_TEACHER_REVIEW")))
                .andReturn().getResponse().getContentAsString();
        JsonNode captured = JsonMapper.builder().findAndAddModules().build().readTree(response);
        String recommendationId = captured.path("data").path("recommendationId").asText();

        mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", captureKey)
                        .contentType("application/json")
                        .content(captureBody(run)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendationId", equalTo(recommendationId)));

        mockMvc.perform(get("/api/v1/teacher/analysis-recommendations/{id}", recommendationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendationId", equalTo(recommendationId)))
                .andExpect(jsonPath("$.data.source", equalTo("SMARTBI_AICHAT")))
                .andExpect(jsonPath("$.data.captureMode", equalTo("MANUAL")))
                .andExpect(jsonPath("$.data.candidates", hasSize(3)))
                .andExpect(jsonPath("$.data.evidenceRefs[0]", equalTo("attempt-101")))
                .andExpect(jsonPath("$.data.candidates[1].strategyCode", equalTo("VISUAL_TRANSFER_PRACTICE")));

        String interventionResponse = mockMvc.perform(post("/api/v1/teacher/interventions")
                        .header("Idempotency-Key", "t07-intervention-1")
                        .contentType("application/json")
                        .content("""
                                {"recommendationId":"%s","strategyCode":"VISUAL_TRANSFER_PRACTICE",
                                 "teacherRationale":"小明当前主要问题是遍历过程混淆，先用可视化迁移题验证。"}
                                """.formatted(recommendationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("PROPOSED")))
                .andExpect(jsonPath("$.data.predictedLift").isNumber())
                .andExpect(jsonPath("$.data.predictionInterval.low").isNumber())
                .andExpect(jsonPath("$.data.predictionInterval.high").isNumber())
                .andExpect(jsonPath("$.data.version", equalTo(1)))
                .andReturn().getResponse().getContentAsString();
        JsonNode intervention = JsonMapper.builder().findAndAddModules().build().readTree(interventionResponse);
        String interventionId = intervention.path("data").path("interventionId").asText();
        double predictedLift = intervention.path("data").path("predictedLift").asDouble();
        assertThat(predictedLift).isGreaterThanOrEqualTo(0.0).isLessThanOrEqualTo(1.0);

        mockMvc.perform(post("/api/v1/teacher/interventions/{id}/approve", interventionId)
                        .header("Idempotency-Key", "t07-approve-missing-if-match"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.code", equalTo("PRECONDITION_FAILED")));

        mockMvc.perform(post("/api/v1/teacher/interventions/{id}/approve", interventionId)
                        .header("Idempotency-Key", "t07-approve-1")
                        .header("If-Match", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("APPROVED")))
                .andExpect(jsonPath("$.data.version", equalTo(2)));

        String commitResponse = mockMvc.perform(post("/api/v1/teacher/interventions/{id}/commit", interventionId)
                        .header("Idempotency-Key", "t07-commit-1")
                        .header("If-Match", "2")
                        .contentType("application/json")
                        .content("{\"dueAt\":\"2026-08-29T18:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("COMMITTED")))
                .andExpect(jsonPath("$.data.assignmentId").isString())
                .andExpect(jsonPath("$.data.practiceSetId").isString())
                .andExpect(jsonPath("$.data.version", equalTo(3)))
                .andReturn().getResponse().getContentAsString();
        JsonNode committed = JsonMapper.builder().findAndAddModules().build().readTree(commitResponse);
        String assignmentId = committed.path("data").path("assignmentId").asText();
        String practiceSetId = committed.path("data").path("practiceSetId").asText();

        mockMvc.perform(post("/api/v1/teacher/interventions/{id}/commit", interventionId)
                        .header("Idempotency-Key", "t07-commit-1")
                        .header("If-Match", "2")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignmentId", equalTo(assignmentId)))
                .andExpect(jsonPath("$.data.practiceSetId", equalTo(practiceSetId)));

        assertThat(jdbcTemplate.queryForObject("select count(*) from app.intervention_assignments where assignment_id = ?", Integer.class, assignmentId))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select source from app.practice_sets where practice_set_id = ?", String.class, practiceSetId))
                .isEqualTo("TEACHER_ASSIGNMENT");
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_intervention where intervention_id = ?", Integer.class, interventionId))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select assignment_id from smartbi_exchange.sb_fact_intervention where intervention_id = ?", String.class, interventionId))
                .isEqualTo(assignmentId);
        assertThat(jdbcTemplate.queryForObject("select count(*) from app.analysis_recommendation_candidates where recommendation_id = ?", Integer.class, recommendationId))
                .isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("select source_snapshot from app.analysis_recommendation_candidates where recommendation_id = ? and candidate_index = 2", String.class, recommendationId))
                .contains("VISUAL_TRANSFER_PRACTICE");
    }

    @Test
    void rejectsStaleVersionAndDoesNotTreatAiChatAsApprovedAction() throws Exception {
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String response = mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", "t07-capture-2")
                        .contentType("application/json")
                        .content(captureBody(run)))
                .andReturn().getResponse().getContentAsString();
        String recommendationId = JsonMapper.builder().findAndAddModules().build().readTree(response).path("data").path("recommendationId").asText();

        String intervention = mockMvc.perform(post("/api/v1/teacher/interventions")
                        .header("Idempotency-Key", "t07-intervention-2")
                        .contentType("application/json")
                        .content("""
                                {"recommendationId":"%s","strategyCode":"CONCEPT_REMEDIATION",
                                 "teacherRationale":"教师确认先做概念边界辨析并观察迁移效果。"}
                                """.formatted(recommendationId)))
                .andReturn().getResponse().getContentAsString();
        String interventionId = JsonMapper.builder().findAndAddModules().build().readTree(intervention).path("data").path("interventionId").asText();

        mockMvc.perform(post("/api/v1/teacher/interventions/{id}/approve", interventionId)
                        .header("Idempotency-Key", "t07-approve-stale")
                        .header("If-Match", "99"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.code", equalTo("PRECONDITION_FAILED")));
        assertThat(jdbcTemplate.queryForObject("select status from app.interventions where intervention_id = ?", String.class, interventionId))
                .isEqualTo("PROPOSED");
    }

    private String captureBody(DemoRun run) {
        return """
                {"studentId":"stu-xiaoming","courseId":"course-data-structures","classId":"class-cs-2024-01",
                 "knowledgePointId":"kp-graph-bfs-dfs","demoRunId":"%s","demoCaseId":"DEMO-GRAPH-001",
                 "correlationId":"%s","analysisSummary":"两道诊断题均错误，主要集中在访问顺序和回溯。",
                 "evidenceRefs":["attempt-101","attempt-102"],
                 "candidates":[
                   {"strategyCode":"CONCEPT_REMEDIATION","title":"方案A","rationale":"先纠正概念边界","actionDescription":"概念辨析+低难度专项"},
                   {"strategyCode":"VISUAL_TRANSFER_PRACTICE","title":"方案B","rationale":"用可视化过程建立正确迁移","actionDescription":"BFS/DFS过程演示+变式练习"},
                   {"strategyCode":"AI_GUIDED_VARIATION","title":"方案C","rationale":"持续个性化反馈","actionDescription":"AI Coach引导+分层变式"}],
                 "source":"SMARTBI_AICHAT"}
                """.formatted(run.demoRunId(), run.correlationId());
    }
}
