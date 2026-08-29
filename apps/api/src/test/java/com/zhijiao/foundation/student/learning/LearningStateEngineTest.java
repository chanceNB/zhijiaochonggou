package com.zhijiao.foundation.student.learning;

import com.zhijiao.foundation.demo.BaselineSeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LearningStateEngineTest {

    private static final String BASELINE_VERSION = "baseline-ds-v1";
    private static final String COURSE_ID = "course-data-structures";
    private static final String XIAOMING_ID = "stu-xiaoming";
    private static final String BFS_DFS_ID = "kp-graph-bfs-dfs";

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
    }

    @Test
    void recomputePersistsDerivedStateAndRanksXiaomingWeakPoint() {
        learningStateEngine.recompute(BASELINE_VERSION);

        Map<String, Object> state = jdbcTemplate.queryForMap("""
                select mastery, confidence, forgetting_risk, evidence_count,
                       last_evidence_at, mastery_model_version, ability_model_version,
                       forgetting_model_version, confidence_model_version, computed_at
                from app.learning_snapshots
                where student_id = ? and course_id = ? and knowledge_point_id = ?
                """, XIAOMING_ID, COURSE_ID, BFS_DFS_ID);
        Map<String, Object> candidate = jdbcTemplate.queryForMap("""
                select weakness_score, confidence, evidence_count, rank_position, reason_codes
                from app.weak_knowledge_point_candidates
                where student_id = ? and course_id = ? and knowledge_point_id = ?
                """, XIAOMING_ID, COURSE_ID, BFS_DFS_ID);

        assertThat(((Number) state.get("mastery")).doubleValue()).isBetween(0.0, 1.0);
        assertThat(((Number) state.get("confidence")).doubleValue()).isBetween(0.0, 1.0);
        assertThat(((Number) state.get("forgetting_risk")).doubleValue()).isBetween(0.0, 1.0);
        assertThat(state.get("evidence_count")).isEqualTo(5);
        assertThat(state.get("mastery_model_version")).isEqualTo("BKT_V1_FIXED_PARAMS");
        assertThat(state.get("ability_model_version")).isEqualTo("RASCH_MAP_V1");
        assertThat(state.get("forgetting_model_version")).isEqualTo("RECENCY_GAP_V1");
        assertThat(state.get("confidence_model_version")).isEqualTo("STATE_CONFIDENCE_V1");
        assertThat(state.get("computed_at")).isNotNull();
        assertThat(jdbcTemplate.queryForObject("""
                select source_version from app.weak_knowledge_point_candidates
                where student_id = ? and course_id = ? and knowledge_point_id = ?
                """, String.class, XIAOMING_ID, COURSE_ID, BFS_DFS_ID))
                .isEqualTo(BASELINE_VERSION);
        assertThat(((Number) candidate.get("weakness_score")).doubleValue()).isBetween(0.0, 1.0);
        assertThat(candidate.get("evidence_count")).isEqualTo(5);
        assertThat(((Number) candidate.get("rank_position")).intValue()).isEqualTo(1);
        assertThat(candidate.get("reason_codes").toString()).contains("LOW_MASTERY", "REPEATED_RECENT_ERRORS");
    }

    @Test
    void recomputationIsDeterministicAndDoesNotUseLearningEventAsAssessment() {
        learningStateEngine.recompute(BASELINE_VERSION);
        Map<String, Object> first = jdbcTemplate.queryForMap("""
                select mastery, confidence, forgetting_risk, evidence_count, computed_at
                from app.learning_snapshots
                where student_id = ? and knowledge_point_id = ?
                """, XIAOMING_ID, BFS_DFS_ID);
        long firstRows = jdbcTemplate.queryForObject("select count(*) from app.learning_snapshots", Long.class);

        learningStateEngine.recompute(BASELINE_VERSION);

        Map<String, Object> second = jdbcTemplate.queryForMap("""
                select mastery, confidence, forgetting_risk, evidence_count, computed_at
                from app.learning_snapshots
                where student_id = ? and knowledge_point_id = ?
                """, XIAOMING_ID, BFS_DFS_ID);
        long secondRows = jdbcTemplate.queryForObject("select count(*) from app.learning_snapshots", Long.class);

        assertThat(first).isEqualTo(second);
        assertThat(secondRows).isEqualTo(firstRows);
        assertThat(jdbcTemplate.queryForObject("select count(*) from app.learning_events where mastery_after is not null", Long.class))
                .isZero();
    }
}
