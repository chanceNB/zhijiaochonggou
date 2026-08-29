package com.zhijiao.foundation.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BaselineSeedServiceTest {

    private static final String BASELINE_VERSION = "baseline-ds-v1";
    private static final String COURSE_ID = "course-data-structures";
    private static final String XIAOMING_ID = "stu-xiaoming";
    private static final String BFS_DFS_ID = "kp-graph-bfs-dfs";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private DemoRunService demoRunService;

    @BeforeEach
    void cleanDatabase() {
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
    }

    @Test
    void seedCreatesDeterministicGoldenBaseline() {
        baselineSeedService.seed();

        assertThat(count("app.courses")).isEqualTo(1);
        assertThat(count("app.classrooms")).isEqualTo(2);
        assertThat(count("app.students")).isEqualTo(80);
        assertThat(countWhere("app.students", "student_id = 'stu-xiaoming'")).isEqualTo(1);
        assertThat(value("select class_id from app.students where student_id = 'stu-xiaoming'", String.class))
                .isEqualTo("class-cs-2024-01");
        assertThat(value("select course_id from app.courses where course_id = 'course-data-structures'", String.class))
                .isEqualTo(COURSE_ID);
        assertThat(count("app.knowledge_points")).isGreaterThanOrEqualTo(10);
        assertThat(countWhere("app.learning_events", "student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-graph-bfs-dfs'"))
                .isGreaterThanOrEqualTo(3);
        assertThat(countWhere("app.practice_attempts", "student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-graph-bfs-dfs'"))
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void seedIsIdempotentAndKeepsBaselineVersionStable() {
        baselineSeedService.seed();
        long students = count("app.students");
        long learningEvents = count("app.learning_events");
        long attempts = count("app.practice_attempts");
        long questionItems = count("app.question_items");

        baselineSeedService.seed();

        assertThat(count("app.students")).isEqualTo(students);
        assertThat(count("app.learning_events")).isEqualTo(learningEvents);
        assertThat(count("app.practice_attempts")).isEqualTo(attempts);
        assertThat(count("app.question_items")).isEqualTo(questionItems);
        assertThat(value("select baseline_version from app.baseline_metadata", String.class))
                .isEqualTo(BASELINE_VERSION);
        assertThat(countWhere("app.learning_events", "data_origin <> 'BASELINE_SIMULATED'" )).isZero();
        assertThat(countWhere("app.practice_attempts", "data_origin <> 'BASELINE_SIMULATED'" )).isZero();
        assertThat(countWhere("app.question_items", "data_origin <> 'BASELINE_SIMULATED'" )).isZero();
    }

    @Test
    void t01DoesNotSeedAuthoritativeAlgorithmState() {
        baselineSeedService.seed();

        assertThat(count("app.learning_snapshots")).isZero();
        assertThat(countWhere("app.learning_events", "mastery_after is not null")).isZero();
        assertThat(countWhere("app.learning_events", "confidence is not null")).isZero();
        assertThat(countWhere("app.learning_events", "forgetting_risk is not null")).isZero();
    }

    @Test
    void xiaomingRawBfsEvidenceIsMeaningfullyWeaker() {
        baselineSeedService.seed();

        double bfsErrorRate = value("""
                select avg(case when correct then 0.0 else 1.0 end)
                from app.practice_attempts
                where student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-graph-bfs-dfs'
                """, Double.class);
        double linearListErrorRate = value("""
                select avg(case when correct then 0.0 else 1.0 end)
                from app.practice_attempts
                where student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-sorting'
                """, Double.class);

        assertThat(countWhere("app.practice_attempts",
                "student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-graph-bfs-dfs' and correct = false"))
                .isGreaterThanOrEqualTo(4);
        assertThat(bfsErrorRate).isGreaterThan(linearListErrorRate + 0.25d);
        assertThat(value("""
                select avg(response_time_ms) from app.practice_attempts
                where student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-graph-bfs-dfs'
                """, Double.class)).isGreaterThan(value("""
                select avg(response_time_ms) from app.practice_attempts
                where student_id = 'stu-xiaoming' and knowledge_point_id = 'kp-sorting'
                """, Double.class));
    }

    @Test
    void baselineHistorySpansThirtyDays() {
        baselineSeedService.seed();

        Map<String, Object> bounds = jdbcTemplate.queryForMap(
                "select min(event_time) as oldest, max(event_time) as newest from app.learning_events");
        Instant oldest = toInstant(bounds.get("oldest"));
        Instant newest = toInstant(bounds.get("newest"));

        assertThat(Duration.between(oldest, newest).toDays()).isEqualTo(29);
        assertThat(countWhere("app.learning_events",
                "event_time < timestamp with time zone '2026-07-31 00:00:00+00'" )).isZero();
        assertThat(countWhere("app.learning_events",
                "event_time >= timestamp with time zone '2026-08-30 00:00:00+00'" )).isZero();
        assertThat(countWhere("app.practice_attempts",
                "attempt_time < timestamp with time zone '2026-07-31 00:00:00+00'" )).isZero();
        assertThat(countWhere("app.practice_attempts",
                "attempt_time >= timestamp with time zone '2026-08-30 00:00:00+00'" )).isZero();
    }

    @Test
    void rawFactsContainFutureAlgorithmInputsAndQuestionMetadata() {
        baselineSeedService.seed();

        long attempts = count("app.practice_attempts");
        assertThat(countWhere("app.practice_attempts", "response_time_ms > 0")).isEqualTo(attempts);
        assertThat(countWhere("app.practice_attempts", "attempt_index >= 1")).isEqualTo(attempts);
        assertThat(countWhere("app.practice_attempts", "difficulty is not null and question_id is not null")).isEqualTo(attempts);
        assertThat(countWhere("app.practice_attempts", "attempt_time is not null and data_origin = 'BASELINE_SIMULATED'"))
                .isEqualTo(attempts);
        assertThat(count("app.question_items")).isGreaterThan(10);
        assertThat(value("""
                select count(*) from app.practice_attempts p
                join app.question_items q on q.question_id = p.question_id
                where p.difficulty = q.difficulty
                """, Long.class)).isEqualTo(attempts);
        assertThat(countWhere("app.question_items", "data_origin <> 'BASELINE_SIMULATED'" )).isZero();
    }

    @Test
    void resetCreatesNewRunWithoutDeletingBaselineOrMixingLiveData() {
        baselineSeedService.seed();
        long baselineEvents = countWhere("app.learning_events", "data_origin = 'BASELINE_SIMULATED'");
        DemoRun first = demoRunService.create("DEMO-GRAPH-001", BASELINE_VERSION);

        jdbcTemplate.update("""
                insert into app.learning_events
                (event_id, event_time, student_id, course_id, class_id, knowledge_point_id,
                 event_type, correct, mastery_after, confidence, forgetting_risk,
                 data_origin, source_version, baseline_version, demo_run_id, demo_case_id, correlation_id, ingested_at)
                values (?, timestamp with time zone '2026-08-29 10:00:00+00', ?, ?, ?, ?,
                        'LIVE_PRACTICE', false, null, null, null,
                        'LIVE_DEMO', 'live-v1', ?, ?, ?, ?, timestamp with time zone '2026-08-29 10:00:00+00')
                """, "live-event-001", XIAOMING_ID, COURSE_ID, "class-cs-2024-01", BFS_DFS_ID,
                BASELINE_VERSION, first.demoRunId(), "DEMO-GRAPH-001", first.correlationId());

        DemoRun second = demoRunService.reset(first.demoRunId());

        assertThat(second.demoRunId()).isNotEqualTo(first.demoRunId());
        assertThat(value("select status from app.demo_runs where demo_run_id = ?", String.class, first.demoRunId()))
                .isEqualTo("RESET");
        assertThat(value("select status from app.demo_runs where demo_run_id = ?", String.class, second.demoRunId()))
                .isEqualTo("ACTIVE");
        assertThat(countWhere("app.learning_events", "data_origin = 'BASELINE_SIMULATED'" )).isEqualTo(baselineEvents);
        assertThat(countWhere("app.learning_events", "data_origin = 'LIVE_DEMO'" )).isEqualTo(1);
        assertThat(countWhere("app.learning_events", "data_origin = 'BASELINE_SIMULATED' and demo_run_id is not null" )).isZero();
    }

    private long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }

    private long countWhere(String table, String predicate) {
        return jdbcTemplate.queryForObject("select count(*) from " + table + " where " + predicate, Long.class);
    }

    private <T> T value(String sql, Class<T> type, Object... args) {
        return jdbcTemplate.queryForObject(sql, type, args);
    }

    private Instant toInstant(Object value) {
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        return ((Timestamp) value).toInstant();
    }
}
