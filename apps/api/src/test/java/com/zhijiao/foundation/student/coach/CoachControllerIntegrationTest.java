package com.zhijiao.foundation.student.coach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.knowledge.KnowledgeIngestionService;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoachControllerIntegrationTest {
    private static final String COURSE_ID = "course-data-structures";
    private static final String KP_ID = "kp-graph-bfs-dfs";

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired KnowledgeIngestionService knowledgeIngestionService;
    @MockBean LlmPort llmPort;

    @BeforeEach
    void reset() {
        jdbcTemplate.update("delete from app.coach_citations");
        jdbcTemplate.update("delete from app.coach_diagnostic_questions");
        jdbcTemplate.update("delete from app.coach_messages");
        jdbcTemplate.update("delete from app.coach_sessions");
        jdbcTemplate.update("delete from app.knowledge_chunks");
        jdbcTemplate.update("delete from app.knowledge_documents");
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates");
        jdbcTemplate.update("delete from app.student_learning_abilities");
        jdbcTemplate.update("delete from app.learning_snapshots");
        jdbcTemplate.update("delete from app.demo_runs");
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
        knowledgeIngestionService.ingestText(COURSE_ID, "数据结构课程讲义", "fixture://ds",
                "BFS 使用队列维护待访问顶点，DFS 通过递归和回溯探索路径。", KP_ID,
                BaselineSeedService.SOURCE_VERSION, BaselineSeedService.DATA_ORIGIN);
    }

    @Test
    void createsIdempotentSessionAndRecoversLearningContext() throws Exception {
        String first = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "coach-create-1")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.data.context.mastery", notNullValue()))
                .andExpect(jsonPath("$.data.dataOrigin").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(first).path("data").path("sessionId").asText();

        String second = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "coach-create-1")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(JsonMapper.builder().findAndAddModules().build()
                .readTree(second).path("data").path("sessionId").asText()).isEqualTo(sessionId);

        mockMvc.perform(get("/api/v1/student/coach/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId", equalTo(sessionId)))
                .andExpect(jsonPath("$.data.context.weaknessScore", notNullValue()));
    }

    @Test
    void requiresExplicitStudentIdentity() throws Exception {
        mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "coach-create-missing-student")
                        .contentType("application/json")
                        .content("{\"courseId\":\"course-data-structures\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("VALIDATION_ERROR")));
    }

    @Test
    void messageStoresRealCitationAndDiagnosticSetStoresExactlyTwoQuestions() throws Exception {
        when(llmPort.complete(any())).thenReturn(new LlmResponse("BFS 使用队列可以按层维护待访问节点。", "test", "test-model", "coach-prompt-v1"));
        String response = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "coach-create-2")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"mode\":\"DIAGNOSTIC\"}"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(response).path("data").path("sessionId").asText();

        mockMvc.perform(post("/api/v1/student/coach/sessions/{sessionId}/messages", sessionId)
                        .header("Idempotency-Key", "coach-message-1")
                        .contentType("application/json")
                        .content("{\"message\":\"为什么 BFS 要用队列？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ragStatus", equalTo("INDEXED")))
                .andExpect(jsonPath("$.data.citations", hasSize(1)))
                .andExpect(jsonPath("$.data.citations[0].chunkId", notNullValue()));

        when(llmPort.complete(any()))
                .thenReturn(new LlmResponse(diagnosticJson(), "test", "test-model", "coach-prompt-v1"));
        mockMvc.perform(post("/api/v1/student/coach/sessions/{sessionId}/diagnostic-sets", sessionId)
                        .header("Idempotency-Key", "coach-diagnostic-1")
                        .contentType("application/json")
                        .content("{\"knowledgePointId\":\"kp-graph-bfs-dfs\",\"questionCount\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionCount", equalTo(2)))
                .andExpect(jsonPath("$.data.questions", hasSize(2)))
                .andExpect(jsonPath("$.data.questions[0].questionId", notNullValue()));

        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.coach_diagnostic_questions", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.coach_citations where practice_set_id is not null", Integer.class)).isEqualTo(2);
    }

    @Test
    void llmFailureLeavesUserMessageAndRagFailureIsExplicitlyDegraded() throws Exception {
        when(llmPort.complete(any())).thenThrow(new LlmUnavailableException("test unavailable"));
        String response = mockMvc.perform(post("/api/v1/student/coach/sessions")
                        .header("Idempotency-Key", "coach-create-3")
                        .contentType("application/json")
                        .content("{\"studentId\":\"stu-xiaoming\",\"courseId\":\"course-data-structures\",\"knowledgePointId\":\"kp-graph-bfs-dfs\"}"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonMapper.builder().findAndAddModules().build().readTree(response).path("data").path("sessionId").asText();
        mockMvc.perform(post("/api/v1/student/coach/sessions/{sessionId}/messages", sessionId)
                        .header("Idempotency-Key", "coach-message-failure")
                        .contentType("application/json")
                        .content("{\"message\":\"请解释 BFS\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code", equalTo("AI_UPSTREAM_ERROR")));
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from app.coach_messages where session_id = ? and message_type = 'USER'", Integer.class, sessionId)).isEqualTo(1);
    }

    private String diagnosticJson() {
        return """
                {"questions":[
                  {"questionId":"diag-q-1","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"BFS uses which structure?","options":[{"optionId":"A","text":"队列"},{"optionId":"B","text":"栈"}],"correctAnswer":"A","explanation":"队列维护层序访问。","diagnosticTarget":{"code":"BFS_QUEUE_ORDER","description":"访问顺序"},"difficulty":0.5},
                  {"questionId":"diag-q-2","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"DFS traversal may require what?","options":[{"optionId":"A","text":"回溯"},{"optionId":"B","text":"排序"}],"correctAnswer":"A","explanation":"DFS explores and backtracks。","diagnosticTarget":{"code":"DFS_BACKTRACKING","description":"回溯"},"difficulty":0.6}
                ]}
                """;
    }
}
