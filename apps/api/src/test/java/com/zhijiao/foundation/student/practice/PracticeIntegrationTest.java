package com.zhijiao.foundation.student.practice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.demo.DemoRun;
import com.zhijiao.foundation.demo.DemoRunService;
import com.zhijiao.foundation.student.coach.LlmPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PracticeIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired DemoRunService demoRunService;
    @Autowired com.zhijiao.foundation.student.learning.LearningStateEngine learningStateEngine;
    @MockBean LlmPort llmPort;

    @BeforeEach
    void reset() {
        jdbcTemplate.update("delete from app.wrong_book_items");
        jdbcTemplate.update("delete from app.practice_outcomes");
        jdbcTemplate.update("delete from app.practice_attempts where data_origin = 'LIVE_DEMO'");
        jdbcTemplate.update("delete from app.practice_questions");
        jdbcTemplate.update("delete from app.practice_sets");
        jdbcTemplate.update("delete from app.coach_citations");
        jdbcTemplate.update("delete from app.coach_diagnostic_questions");
        jdbcTemplate.update("delete from app.coach_messages");
        jdbcTemplate.update("delete from app.coach_sessions");
        jdbcTemplate.update("delete from app.knowledge_chunks");
        jdbcTemplate.update("delete from app.knowledge_documents");
        jdbcTemplate.update("delete from app.demo_runs");
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates");
        jdbcTemplate.update("delete from app.student_learning_abilities");
        jdbcTemplate.update("delete from app.learning_snapshots");
        jdbcTemplate.update("delete from app.learning_snapshot_history");
        jdbcTemplate.update("delete from app.practice_attempts");
        jdbcTemplate.update("delete from app.learning_events");
        jdbcTemplate.update("delete from app.question_items");
        jdbcTemplate.update("delete from app.knowledge_points");
        jdbcTemplate.update("delete from app.students");
        jdbcTemplate.update("delete from app.classrooms");
        jdbcTemplate.update("delete from app.courses");
        jdbcTemplate.update("delete from app.baseline_metadata");
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
    }

    @Test
    void diagnosticQuestionCanBeAnsweredWithoutLeakingKeyAndWrongBookIsExplicit() throws Exception {
        when(llmPort.complete(any())).thenReturn(new com.zhijiao.foundation.student.coach.LlmResponse(diagnosticJson(), "test", "test", "test"));
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String session = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "t04-session")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(session).path("data").path("sessionId").asText();
        String generated = mockMvc.perform(post("/api/v1/student/coach/sessions/{id}/diagnostic-sets", sessionId)
                        .header("Idempotency-Key", "t04-diagnostic")
                        .contentType("application/json")
                        .content("{\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"questionCount\":2}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode generatedNode = JsonMapper.builder().findAndAddModules().build().readTree(generated);
        String setId = generatedNode.path("data").path("practiceSetId").asText();
        String questionId = generatedNode.path("data").path("questions").get(0).path("questionId").asText();

        mockMvc.perform(get("/api/v1/student/practice-sets/{id}", setId))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("correctAnswer"))))
                .andExpect(content().string(not(containsString("explanation"))))
                .andExpect(jsonPath("$.data.questions", org.hamcrest.Matchers.hasSize(2)));

        String attempt = mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", setId)
                        .header("Idempotency-Key", "t04-attempt")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + questionId + "\",\"answer\":\"B\",\"durationSeconds\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct", equalTo(false)))
                .andExpect(jsonPath("$.data.correctAnswer", equalTo("A")))
                .andReturn().getResponse().getContentAsString();
        String attemptId = JsonMapper.builder().findAndAddModules().build().readTree(attempt).path("data").path("attemptId").asText();

        String wrongBookResponse = mockMvc.perform(post("/api/v1/student/practice-attempts/{id}/wrong-book", attemptId)
                        .header("Idempotency-Key", "t04-wrong")
                        .contentType("application/json").content("{\"reason\":\"混淆访问顺序\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("TO_REVIEW")))
                .andReturn().getResponse().getContentAsString();
        String wrongItemId = JsonMapper.builder().findAndAddModules().build().readTree(wrongBookResponse).path("data").path("wrongItemId").asText();
        mockMvc.perform(post("/api/v1/student/practice-attempts/{id}/wrong-book", attemptId)
                        .header("Idempotency-Key", "t04-wrong-replay")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/student/wrong-book/{id}/review", wrongItemId)
                        .header("Idempotency-Key", "t04-wrong-review")
                        .contentType("application/json").content("{\"answer\":\"A\",\"durationSeconds\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct", equalTo(true)))
                .andExpect(jsonPath("$.data.status", equalTo("LEARNING")))
                .andExpect(jsonPath("$.data.reviewCount", equalTo(1)));

        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", setId)
                        .header("Idempotency-Key", "t04-attempt")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + questionId + "\",\"answer\":\"B\",\"durationSeconds\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptId", equalTo(attemptId)));

        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/complete", setId)
                        .header("Idempotency-Key", "t04-complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptCount", equalTo(1)))
                .andExpect(jsonPath("$.data.learningStateStatus", equalTo("UPDATED")));
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.learning_snapshot_history where student_id = 'stu-xiaoming'", Integer.class)).isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select data_origin from app.practice_attempts where attempt_id = ?", String.class, attemptId)).isEqualTo("LIVE_DEMO");
        assertThat(jdbcTemplate.queryForObject(
                "select data_origin from app.learning_snapshots where student_id = ? and knowledge_point_id = ?",
                String.class, "stu-xiaoming", "kp-graph-bfs-dfs")).isEqualTo("LIVE_DEMO");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.learning_snapshot_history where student_id = ? and data_origin = 'BASELINE_SIMULATED'",
                Integer.class, "stu-xiaoming")).isGreaterThan(0);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.learning_snapshot_history where student_id = ? and data_origin = 'LIVE_DEMO'",
                Integer.class, "stu-xiaoming")).isGreaterThan(0);
    }

    @Test
    void resetRejectsFurtherWritesToAnOldLiveDemoPracticeSet() throws Exception {
        when(llmPort.complete(any())).thenReturn(new com.zhijiao.foundation.student.coach.LlmResponse(diagnosticJson(), "test", "test", "test"));
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String sessionResponse = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "t04-reset-session")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(sessionResponse).path("data").path("sessionId").asText();
        String generated = mockMvc.perform(post("/api/v1/student/coach/sessions/{id}/diagnostic-sets", sessionId)
                        .header("Idempotency-Key", "t04-reset-diagnostic")
                        .contentType("application/json")
                        .content("{\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"questionCount\":2}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode generatedNode = JsonMapper.builder().findAndAddModules().build().readTree(generated);
        String setId = generatedNode.path("data").path("practiceSetId").asText();
        String questionId = generatedNode.path("data").path("questions").get(0).path("questionId").asText();
        String oldAttemptResponse = mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", setId)
                        .header("Idempotency-Key", "t04-reset-attempt")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + questionId + "\",\"answer\":\"B\",\"durationSeconds\":12}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String oldAttemptId = JsonMapper.builder().findAndAddModules().build().readTree(oldAttemptResponse).path("data").path("attemptId").asText();
        mockMvc.perform(post("/api/v1/student/practice-attempts/{id}/wrong-book", oldAttemptId)
                        .header("Idempotency-Key", "t04-reset-wrong-book")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isOk());

        demoRunService.reset(run.demoRunId());

        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", setId)
                        .header("Idempotency-Key", "t04-reset-attempt-2")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + questionId + "\",\"answer\":\"B\",\"durationSeconds\":12}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", equalTo("DOMAIN_RULE_VIOLATION")));
        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/complete", setId)
                        .header("Idempotency-Key", "t04-reset-complete"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", equalTo("DOMAIN_RULE_VIOLATION")));
        mockMvc.perform(get("/api/v1/student/wrong-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", equalTo(0)));
        assertThat(jdbcTemplate.queryForObject("select count(*) from app.wrong_book_items", Integer.class)).isEqualTo(1);
    }

    @Test
    void diagnosticPracticeSetIsBoundToTheActiveDemoAtCreation() throws Exception {
        when(llmPort.complete(any())).thenReturn(new com.zhijiao.foundation.student.coach.LlmResponse(diagnosticJson(), "test", "test", "test"));
        DemoRun run = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String sessionResponse = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "t04-bind-session")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(sessionResponse).path("data").path("sessionId").asText();
        String generated = mockMvc.perform(post("/api/v1/student/coach/sessions/{id}/diagnostic-sets", sessionId)
                        .header("Idempotency-Key", "t04-bind-diagnostic")
                        .contentType("application/json")
                        .content("{\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"questionCount\":2}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String setId = JsonMapper.builder().findAndAddModules().build().readTree(generated).path("data").path("practiceSetId").asText();

        assertThat(jdbcTemplate.queryForObject("select demo_run_id from app.practice_sets where practice_set_id = ?", String.class, setId))
                .isEqualTo(run.demoRunId());

        demoRunService.reset(run.demoRunId());
        String questionId = JsonMapper.builder().findAndAddModules().build().readTree(generated).path("data").path("questions").get(0).path("questionId").asText();
        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", setId)
                        .header("Idempotency-Key", "t04-bind-after-reset")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + questionId + "\",\"answer\":\"B\",\"durationSeconds\":12}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", equalTo("DOMAIN_RULE_VIOLATION")));
    }

    @Test
    void similarQuestionIsPersistedAsAStudentSafePracticeSet() throws Exception {
        when(llmPort.complete(any()))
                .thenReturn(new com.zhijiao.foundation.student.coach.LlmResponse(diagnosticJson(), "test", "test", "test"))
                .thenReturn(new com.zhijiao.foundation.student.coach.LlmResponse(similarJson(), "test", "test", "test"));
        demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        String sessionResponse = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "t04-similar-session")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(sessionResponse).path("data").path("sessionId").asText();
        String generated = mockMvc.perform(post("/api/v1/student/coach/sessions/{id}/diagnostic-sets", sessionId)
                        .header("Idempotency-Key", "t04-similar-diagnostic")
                        .contentType("application/json")
                        .content("{\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"questionCount\":2}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode generatedNode = JsonMapper.builder().findAndAddModules().build().readTree(generated);
        String diagnosticSetId = generatedNode.path("data").path("practiceSetId").asText();
        String sourceQuestionId = generatedNode.path("data").path("questions").get(0).path("questionId").asText();
        String attemptResponse = mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", diagnosticSetId)
                        .header("Idempotency-Key", "t04-similar-source-attempt")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + sourceQuestionId + "\",\"answer\":\"B\",\"durationSeconds\":12}"))
                .andReturn().getResponse().getContentAsString();
        String attemptId = JsonMapper.builder().findAndAddModules().build().readTree(attemptResponse).path("data").path("attemptId").asText();

        String similarResponse = mockMvc.perform(post("/api/v1/student/coach/sessions/{id}/similar-questions", sessionId)
                        .header("Idempotency-Key", "t04-similar-generate")
                        .contentType("application/json")
                        .content("{\"sourceAttemptId\":\"" + attemptId + "\",\"count\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions", org.hamcrest.Matchers.hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        JsonNode similarNode = JsonMapper.builder().findAndAddModules().build().readTree(similarResponse);
        String similarSetId = similarNode.path("data").path("practiceSetId").asText();
        String similarQuestionId = similarNode.path("data").path("questions").get(0).path("questionId").asText();
        mockMvc.perform(get("/api/v1/student/practice-sets/{id}", similarSetId))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("correctAnswer"))))
                .andExpect(content().string(not(containsString("explanation"))));
        assertThat(jdbcTemplate.queryForObject("select source from app.practice_sets where practice_set_id = ?", String.class, similarSetId))
                .isEqualTo("AI_COACH_SIMILAR");
        assertThat(jdbcTemplate.queryForObject("select parent_question_id from app.practice_questions where practice_set_id = ? and question_id = ?",
                String.class, similarSetId, similarQuestionId)).isEqualTo(sourceQuestionId);
        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/attempts", similarSetId)
                        .header("Idempotency-Key", "t04-similar-answer")
                        .contentType("application/json")
                        .content("{\"questionId\":\"" + similarQuestionId + "\",\"answer\":\"A\",\"durationSeconds\":10}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/student/practice-sets/{id}/complete", similarSetId)
                        .header("Idempotency-Key", "t04-similar-complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learningStateStatus", equalTo("UPDATED")));
    }

    private String diagnosticJson() {
        return """
                {"questions":[
                  {"questionId":"t04-q-1","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"BFS uses which structure?","options":[{"optionId":"A","text":"队列"},{"optionId":"B","text":"栈"}],"correctAnswer":"A","explanation":"队列维护层序访问。","diagnosticTarget":{"code":"BFS_QUEUE_ORDER","description":"访问顺序"},"difficulty":0.5},
                  {"questionId":"t04-q-2","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"DFS needs what?","options":[{"optionId":"A","text":"回溯"},{"optionId":"B","text":"排序"}],"correctAnswer":"A","explanation":"DFS 会回溯。","diagnosticTarget":{"code":"DFS_BACKTRACKING","description":"回溯"},"difficulty":0.6}
                ]}
                """;
    }

    private String similarJson() {
        return """
                {"questions":[
                  {"questionId":"similar-q-1","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"Which structure supports breadth-first traversal?","options":[{"optionId":"A","text":"Queue"},{"optionId":"B","text":"Stack"}],"correctAnswer":"A","explanation":"A queue preserves level order.","diagnosticTarget":{"code":"BFS_QUEUE_ORDER","description":"Traversal order"},"difficulty":0.55}
                ]}
                """;
    }
}
