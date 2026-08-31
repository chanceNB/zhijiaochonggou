package com.zhijiao.foundation.analytics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.demo.DemoRun;
import com.zhijiao.foundation.demo.DemoRunService;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class F04SmartbiReportingIntegrationTest {
    private static final String STUDENT_ID = BaselineSeedService.XIAOMING_ID;
    private static final String COURSE_ID = BaselineSeedService.COURSE_ID;
    private static final String KP_ID = BaselineSeedService.BFS_DFS_ID;

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired DemoRunService demoRunService;
    @Autowired AnalyticsProjectionService projectionService;
    @Autowired AnalyticsExportService exportService;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void resetFacts() {
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_analysis_recommendation_candidate");
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_analysis_recommendation");
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_intervention_assignment");
        jdbcTemplate.update("delete from app.intervention_assignments");
        jdbcTemplate.update("delete from app.interventions");
        jdbcTemplate.update("delete from app.analysis_recommendation_candidates");
        jdbcTemplate.update("delete from app.analysis_recommendations");
        jdbcTemplate.update("delete from app.practice_questions");
        jdbcTemplate.update("delete from app.practice_sets");
        jdbcTemplate.update("delete from app.demo_runs");
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
    }

    @AfterEach
    void cleanFactsForSharedTestDatabase() {
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_analysis_recommendation_candidate");
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_analysis_recommendation");
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_intervention_assignment");
        jdbcTemplate.update("delete from app.intervention_assignments");
        jdbcTemplate.update("delete from app.interventions");
        jdbcTemplate.update("delete from app.analysis_recommendation_candidates");
        jdbcTemplate.update("delete from app.analysis_recommendations");
    }

    @Test
    void projectsRecommendationCandidatesInterventionAndAssignmentWithActiveTrace() throws Exception {
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String capture = mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", "f04-capture")
                        .contentType("application/json")
                        .content(captureBody(run)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String recommendationId = read(capture).path("data").path("recommendationId").asText();

        String proposed = mockMvc.perform(post("/api/v1/teacher/interventions")
                        .header("Idempotency-Key", "f04-propose")
                        .contentType("application/json")
                        .content("{\"recommendationId\":\"" + recommendationId
                                + "\",\"strategyCode\":\"VISUAL_TRANSFER_PRACTICE\","
                                + "\"teacherRationale\":\"教师确认需要通过迁移练习验证理解。\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String interventionId = read(proposed).path("data").path("interventionId").asText();
        String approved = mockMvc.perform(post("/api/v1/teacher/interventions/{id}/approve", interventionId)
                        .header("Idempotency-Key", "f04-approve").header("If-Match", "1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String committed = mockMvc.perform(post("/api/v1/teacher/interventions/{id}/commit", interventionId)
                        .header("Idempotency-Key", "f04-commit").header("If-Match", "2")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String assignmentId = read(committed).path("data").path("assignmentId").asText();

        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_analysis_recommendation where recommendation_id = ?", Integer.class, recommendationId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_analysis_recommendation_candidate where recommendation_id = ?", Integer.class, recommendationId)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("select count(distinct strategy_code) from smartbi_exchange.sb_fact_analysis_recommendation_candidate where recommendation_id = ?", Integer.class, recommendationId)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("select demo_run_id from smartbi_exchange.sb_fact_analysis_recommendation where recommendation_id = ?", String.class, recommendationId)).isEqualTo(run.demoRunId());
        assertThat(jdbcTemplate.queryForObject("select demo_run_id from smartbi_exchange.sb_fact_analysis_recommendation_candidate where recommendation_id = ? and candidate_index = 1", String.class, recommendationId)).isEqualTo(run.demoRunId());
        assertThat(jdbcTemplate.queryForObject("select teacher_rationale from smartbi_exchange.sb_fact_intervention where intervention_id = ?", String.class, interventionId)).contains("迁移练习");
        assertThat(jdbcTemplate.queryForObject("select status from smartbi_exchange.sb_fact_intervention_assignment where assignment_id = ?", String.class, assignmentId)).isEqualTo("PENDING_STUDENT");
        assertThat(jdbcTemplate.queryForObject("select is_active_demo_flag from smartbi_exchange.sb_fact_intervention_assignment where assignment_id = ?", Integer.class, assignmentId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select correlation_id from smartbi_exchange.sb_fact_intervention_assignment where assignment_id = ?", String.class, assignmentId)).isEqualTo(run.correlationId());
    }

    @Test
    void catalogFreshnessAndCsvExportIncludeNewReportingDatasets() throws Exception {
        projectionService.refresh();
        assertThat(projectionService.catalog()).extracting(AnalyticsDataset::datasetKey)
                .contains("sb_fact_analysis_recommendation", "sb_fact_analysis_recommendation_candidate", "sb_fact_intervention_assignment");
        assertThat(projectionService.freshness()).extracting(AnalyticsFreshness::datasetKey)
                .contains("sb_fact_analysis_recommendation", "sb_fact_analysis_recommendation_candidate", "sb_fact_intervention_assignment");

        AnalyticsExport export = exportService.create("ACTIVE_DEMO", null, "f04-export");
        assertThat(export.files()).anyMatch(path -> path.endsWith("sb_fact_analysis_recommendation.csv"));
        assertThat(export.files()).anyMatch(path -> path.endsWith("sb_fact_analysis_recommendation_candidate.csv"));
        assertThat(export.files()).anyMatch(path -> path.endsWith("sb_fact_intervention_assignment.csv"));
        String manifest = Files.readString(Path.of(export.manifestPath()));
        assertThat(manifest).contains("sb_fact_analysis_recommendation", "sb_fact_analysis_recommendation_candidate", "sb_fact_intervention_assignment");
    }

    @Test
    void resetDemoRunIsNotActiveInRecommendationReporting() throws Exception {
        DemoRun oldRun = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String capture = mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", "f04-old-capture")
                        .contentType("application/json").content(captureBody(oldRun)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String recommendationId = read(capture).path("data").path("recommendationId").asText();
        DemoRun currentRun = demoRunService.reset(oldRun.demoRunId());
        projectionService.refresh();

        assertThat(jdbcTemplate.queryForObject("select is_active_demo_flag from smartbi_exchange.sb_fact_analysis_recommendation where recommendation_id = ?", Integer.class, recommendationId)).isZero();
        assertThat(jdbcTemplate.queryForObject("select active_flag from smartbi_exchange.sb_demo_run_state where demo_run_id = ?", Integer.class, oldRun.demoRunId())).isZero();
        assertThat(jdbcTemplate.queryForObject("select active_flag from smartbi_exchange.sb_demo_run_state where demo_run_id = ?", Integer.class, currentRun.demoRunId())).isEqualTo(1);

        mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", "f04-reset-rejected")
                        .contentType("application/json").content(captureBody(oldRun)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("active demo run")));
        mockMvc.perform(post("/api/v1/teacher/interventions")
                        .header("Idempotency-Key", "f04-reset-intervention")
                        .contentType("application/json")
                        .content("{\"recommendationId\":\"" + recommendationId
                                + "\",\"strategyCode\":\"CONCEPT_REMEDIATION\","
                                + "\"teacherRationale\":\"教师确认需要先校准概念边界。\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("active demo run")));
    }

    @Test
    void captureRejectsDuplicateCandidateStrategyCodes() throws Exception {
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String body = captureBody(run).replace("AI_GUIDED_VARIATION", "CONCEPT_REMEDIATION");
        mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", "f04-duplicate-strategy")
                        .contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("unique")));
    }

    private String captureBody(DemoRun run) {
        return """
                {"studentId":"%s","courseId":"%s","classId":"class-cs-2024-01","knowledgePointId":"%s",
                 "demoRunId":"%s","demoCaseId":"%s","correlationId":"%s","analysisSummary":"诊断证据显示需要验证迁移能力。",
                 "evidenceRefs":["attempt-101","attempt-102"],
                 "candidates":[
                   {"strategyCode":"CONCEPT_REMEDIATION","title":"方案A","rationale":"先纠正概念边界","actionDescription":"概念辨析"},
                   {"strategyCode":"VISUAL_TRANSFER_PRACTICE","title":"方案B","rationale":"用可视化过程建立迁移","actionDescription":"迁移练习"},
                   {"strategyCode":"AI_GUIDED_VARIATION","title":"方案C","rationale":"持续个性化反馈","actionDescription":"分层变式"}],
                 "source":"SMARTBI_AICHAT"}
                """.formatted(STUDENT_ID, COURSE_ID, KP_ID, run.demoRunId(), run.demoCaseId(), run.correlationId());
    }

    private JsonNode read(String value) throws Exception {
        return JsonMapper.builder().findAndAddModules().build().readTree(value);
    }
}
