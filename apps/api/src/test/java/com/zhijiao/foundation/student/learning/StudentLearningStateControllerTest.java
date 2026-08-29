package com.zhijiao.foundation.student.learning;

import com.zhijiao.foundation.demo.BaselineSeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentLearningStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private LearningStateEngine learningStateEngine;

    @BeforeEach
    void resetFixture() {
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
    }

    @Test
    void learningStateUsesUnifiedEnvelopeAndReturnsDerivedValues() throws Exception {
        mockMvc.perform(get("/api/v1/student/learning-state")
                        .header("X-Request-Id", "req-learning-state")
                        .param("studentId", "stu-xiaoming")
                        .param("courseId", "course-data-structures")
                        .param("knowledgePointId", "kp-graph-bfs-dfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.requestId", equalTo("req-learning-state")))
                .andExpect(jsonPath("$.data.studentId", equalTo("stu-xiaoming")))
                .andExpect(jsonPath("$.data.mastery", org.hamcrest.Matchers.greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.data.mastery", org.hamcrest.Matchers.lessThanOrEqualTo(1.0)))
                .andExpect(jsonPath("$.data.theta", notNullValue()))
                .andExpect(jsonPath("$.data.weakKnowledgePoints", org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.greaterThan(0))))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void growthUsesUnifiedEnvelopeAndDoesNotInventInterventionData() throws Exception {
        mockMvc.perform(get("/api/v1/student/growth")
                        .header("X-Request-Id", "req-growth")
                        .param("studentId", "stu-xiaoming")
                        .param("courseId", "course-data-structures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.requestId", equalTo("req-growth")))
                .andExpect(jsonPath("$.data.mastery", notNullValue()))
                .andExpect(jsonPath("$.data.trend", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.completedTasks", equalTo(0)))
                .andExpect(jsonPath("$.data.latestIntervention").doesNotExist());
    }
}
