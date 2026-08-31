package com.zhijiao.foundation.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.demo.DemoRun;
import com.zhijiao.foundation.demo.DemoRunService;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import com.zhijiao.foundation.student.practice.InternalQuestion;
import com.zhijiao.foundation.student.practice.PracticeRepository;
import com.zhijiao.foundation.student.practice.PracticeSet;
import com.zhijiao.foundation.student.practice.QuestionOptionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T08OutcomeGrowthIntegrationTest {
    private static final String STUDENT_ID = "stu-xiaoming";
    private static final String COURSE_ID = "course-data-structures";
    private static final String KNOWLEDGE_POINT_ID = "kp-graph-bfs-dfs";

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired DemoRunService demoRunService;
    @Autowired PracticeRepository practiceRepository;

    @BeforeEach
    void resetFixture() {
        jdbcTemplate.update("delete from app.intervention_assignments");
        jdbcTemplate.update("delete from app.interventions");
        jdbcTemplate.update("delete from app.analysis_recommendation_candidates");
        jdbcTemplate.update("delete from app.analysis_recommendations");
        jdbcTemplate.update("delete from app.practice_outcomes");
        jdbcTemplate.update("delete from app.practice_attempts where data_origin = 'LIVE_DEMO'");
        jdbcTemplate.update("delete from app.practice_questions");
        jdbcTemplate.update("delete from app.practice_sets");
        jdbcTemplate.update("delete from app.demo_runs");
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates");
        jdbcTemplate.update("delete from app.student_learning_abilities");
        jdbcTemplate.update("delete from app.learning_snapshots");
        jdbcTemplate.update("delete from app.learning_snapshot_history");
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
    }

    @Test
    void committedTeacherAssignmentIsExecutableAndProducesOutcomeAndGrowth() throws Exception {
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        seedStructuredSourceQuestions(run);
        String recommendationId = captureRecommendation(run);
        String interventionId = proposeApproveCommit(recommendationId);

        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select demo_run_id from app.intervention_assignments where intervention_id = ?", String.class, interventionId))
                .isEqualTo(run.demoRunId());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select demo_case_id from app.intervention_assignments where intervention_id = ?", String.class, interventionId))
                .isEqualTo(run.demoCaseId());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select correlation_id from app.intervention_assignments where intervention_id = ?", String.class, interventionId))
                .isEqualTo(run.correlationId());

        String today = mockMvc.perform(get("/api/v1/student/today")
                        .queryParam("studentId", STUDENT_ID).queryParam("courseId", COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextAction.title", equalTo("方案B")))
                .andExpect(jsonPath("$.data.nextAction.knowledgePointName", equalTo("图遍历 BFS / DFS")))
                .andExpect(jsonPath("$.data.teacherAssignment.assignmentId").isString())
                .andExpect(jsonPath("$.data.teacherAssignment.practiceSetId").isString())
                .andExpect(jsonPath("$.data.teacherAssignment.title", equalTo("方案B")))
                .andExpect(jsonPath("$.data.teacherAssignment.knowledgePointName", equalTo("图遍历 BFS / DFS")))
                .andExpect(jsonPath("$.data.teacherAssignment.source", equalTo("TEACHER_INTERVENTION")))
                .andExpect(jsonPath("$.data.teacherAssignment.status", equalTo("PENDING_STUDENT")))
                .andReturn().getResponse().getContentAsString();
        String practiceSetId = read(today).path("data").path("teacherAssignment").path("practiceSetId").asText();

        mockMvc.perform(get("/api/v1/student/practice-sets/{id}", practiceSetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source", equalTo("TEACHER_ASSIGNMENT")))
                .andExpect(jsonPath("$.data.questions", hasSize(2)));
        String firstQuestion = jdbcTemplate.queryForObject(
                "select question_id from app.practice_questions where practice_set_id = ? and validation_role = 'DIAGNOSTIC'",
                String.class, practiceSetId);
        String transferQuestion = jdbcTemplate.queryForObject(
                "select question_id from app.practice_questions where practice_set_id = ? and validation_role = 'TRANSFER'",
                String.class, practiceSetId);
        String firstAnswer = jdbcTemplate.queryForObject(
                "select correct_answer from app.practice_questions where practice_set_id = ? and question_id = ?",
                String.class, practiceSetId, firstQuestion);
        String transferAnswer = jdbcTemplate.queryForObject(
                "select correct_answer from app.practice_questions where practice_set_id = ? and question_id = ?",
                String.class, practiceSetId, transferQuestion);

        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", practiceSetId)
                        .header("Idempotency-Key", "t08-attempt-diagnostic")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + firstQuestion + "\",\"answer\":\"" + firstAnswer + "\",\"durationSeconds\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct", equalTo(true)));
        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", practiceSetId)
                        .header("Idempotency-Key", "t08-attempt-transfer")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + transferQuestion + "\",\"answer\":\"" + transferAnswer + "\",\"durationSeconds\":24}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct", equalTo(true)));

        String completed = mockMvc.perform(post("/api/v1/student/practice-sets/{id}/complete", practiceSetId)
                        .header("Idempotency-Key", "t08-complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learningStateStatus", equalTo("UPDATED")))
                .andExpect(jsonPath("$.data.transferValidation", equalTo("PASS")))
                .andExpect(jsonPath("$.data.learningStateAfter.mastery").isNumber())
                .andReturn().getResponse().getContentAsString();
        String outcomeId = read(completed).path("data").path("outcomeId").asText();

        mockMvc.perform(get("/api/v1/teacher/interventions/{id}/outcome", interventionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.outcomeId", equalTo(outcomeId)))
                .andExpect(jsonPath("$.data.predictedLift").isNumber())
                .andExpect(jsonPath("$.data.actualLift").isNumber())
                .andExpect(jsonPath("$.data.predictionDeviation").isNumber())
                .andExpect(jsonPath("$.data.masteryBefore").isNumber())
                .andExpect(jsonPath("$.data.masteryAfter").isNumber())
                .andExpect(jsonPath("$.data.transferValidation", equalTo("PASS")));

        String replayed = mockMvc.perform(post("/api/v1/student/practice-sets/{id}/complete", practiceSetId)
                        .header("Idempotency-Key", "t08-complete-replay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.outcomeId", equalTo(outcomeId)))
                .andExpect(jsonPath("$.data.transferValidation", equalTo("PASS")))
                .andExpect(jsonPath("$.data.learningStateAfter.mastery").isNumber())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(read(replayed).path("data").path("interventionOutcomeId").asText())
                .isEqualTo(outcomeId);

        mockMvc.perform(get("/api/v1/student/growth")
                        .queryParam("studentId", STUDENT_ID).queryParam("courseId", COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latestIntervention.strategyCode", equalTo("VISUAL_TRANSFER_PRACTICE")))
                .andExpect(jsonPath("$.data.latestIntervention.predictedLift").isNumber())
                .andExpect(jsonPath("$.data.latestIntervention.actualLift").isNumber())
                .andExpect(jsonPath("$.data.latestIntervention.predictionDeviation").isNumber())
                .andExpect(jsonPath("$.data.latestIntervention.transferValidation", equalTo("PASS")));

        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select count(*) from smartbi_exchange.sb_fact_intervention_outcome where intervention_id = ?",
                Integer.class, interventionId)).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.intervention_outcomes where outcome_id = ?", Integer.class, outcomeId)).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select status from app.intervention_assignments where intervention_id = ?", String.class, interventionId))
                .isEqualTo("COMPLETED");
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select status from smartbi_exchange.sb_fact_intervention_assignment where intervention_id = ?", String.class, interventionId))
                .isEqualTo("COMPLETED");
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select is_active_demo_flag from smartbi_exchange.sb_fact_intervention_assignment where intervention_id = ?", Integer.class, interventionId))
                .isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select demo_run_id from app.intervention_outcomes where intervention_id = ?", String.class, interventionId))
                .isEqualTo(run.demoRunId());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select correlation_id from app.intervention_outcomes where intervention_id = ?", String.class, interventionId))
                .isEqualTo(run.correlationId());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select demo_run_id from smartbi_exchange.sb_fact_intervention_outcome where intervention_id = ?", String.class, interventionId))
                .isEqualTo(run.demoRunId());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select actual_lift from smartbi_exchange.sb_fact_intervention_outcome where intervention_id = ?", Double.class, interventionId))
                .isNotNull();
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select count(*) from smartbi_exchange.sb_fact_learning_state where demo_run_id = ? and snapshot_status = 'CURRENT'", Integer.class, run.demoRunId()))
                .isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select count(*) from smartbi_exchange.sb_fact_learning_state where student_id = ? and knowledge_point_id = ? and snapshot_status = 'HISTORICAL'", Integer.class, STUDENT_ID, KNOWLEDGE_POINT_ID))
                .isGreaterThan(0);
    }

    private void seedStructuredSourceQuestions(DemoRun run) {
        practiceRepository.insertPracticeSet(new PracticeSet("ps-source-t08", STUDENT_ID, COURSE_ID,
                "class-cs-2024-01", null, "AI_COACH_DIAGNOSTIC", "OPEN", run.demoRunId(),
                run.demoCaseId(), run.correlationId(), BaselineSeedService.SOURCE_VERSION, Instant.now(), null),
                List.of(
                        question("source-q-1", "BFS uses which structure?", "A", 0.55),
                        question("source-q-2", "DFS uses which structure?", "A", 0.65)), List.of());
    }

    private InternalQuestion question(String id, String stem, String answer, double difficulty) {
        return new InternalQuestion(id, "ps-source-t08", "AI_COACH_DIAGNOSTIC", null, KNOWLEDGE_POINT_ID,
                "SINGLE_CHOICE", stem, List.of(new QuestionOptionView("A", "Queue"), new QuestionOptionView("B", "Stack")),
                answer, "Structured source question", difficulty, Instant.now());
    }

    private String captureRecommendation(DemoRun run) throws Exception {
        String body = """
                {"studentId":"%s","courseId":"%s","classId":"class-cs-2024-01","knowledgePointId":"%s",
                 "demoRunId":"%s","demoCaseId":"%s","correlationId":"%s","analysisSummary":"教师需要验证图遍历迁移表现",
                 "candidates":[
                   {"strategyCode":"CONCEPT_REMEDIATION","title":"方案A","rationale":"先校准概念边界","actionDescription":"概念辨析"},
                   {"strategyCode":"VISUAL_TRANSFER_PRACTICE","title":"方案B","rationale":"通过迁移任务观察过程","actionDescription":"可视化迁移练习"},
                   {"strategyCode":"AI_GUIDED_VARIATION","title":"方案C","rationale":"使用分层变式持续反馈","actionDescription":"分层变式练习"}],
                 "source":"SMARTBI_AICHAT"}
                """.formatted(STUDENT_ID, COURSE_ID, KNOWLEDGE_POINT_ID, run.demoRunId(), run.demoCaseId(), run.correlationId());
        String response = mockMvc.perform(post("/api/v1/teacher/analysis-recommendations")
                        .header("Idempotency-Key", "t08-capture")
                        .contentType("application/json").content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return read(response).path("data").path("recommendationId").asText();
    }

    private String proposeApproveCommit(String recommendationId) throws Exception {
        String proposed = mockMvc.perform(post("/api/v1/teacher/interventions")
                        .header("Idempotency-Key", "t08-propose")
                        .contentType("application/json")
                        .content("{\"recommendationId\":\"" + recommendationId + "\",\"strategyCode\":\"VISUAL_TRANSFER_PRACTICE\",\"teacherRationale\":\"教师确认优先验证图遍历迁移能力。\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String interventionId = read(proposed).path("data").path("interventionId").asText();
        mockMvc.perform(post("/api/v1/teacher/interventions/{id}/approve", interventionId)
                        .header("Idempotency-Key", "t08-approve").header("If-Match", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/teacher/interventions/{id}/commit", interventionId)
                        .header("Idempotency-Key", "t08-commit").header("If-Match", "2")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        return interventionId;
    }

    private JsonNode read(String json) throws Exception {
        return JsonMapper.builder().findAndAddModules().build().readTree(json);
    }
}
