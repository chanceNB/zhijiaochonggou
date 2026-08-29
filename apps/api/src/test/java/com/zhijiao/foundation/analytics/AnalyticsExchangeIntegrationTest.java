package com.zhijiao.foundation.analytics;

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

import java.time.OffsetDateTime;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsExchangeIntegrationTest {
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired DemoRunService demoRunService;
    @Autowired AnalyticsProjectionService projectionService;
    @Autowired AnalyticsExportService exportService;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void reset() {
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_wrong_book");
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_practice_attempt");
        jdbcTemplate.update("delete from smartbi_exchange.sb_fact_learning_state");
        jdbcTemplate.update("delete from smartbi_exchange.sb_demo_run_state");
        jdbcTemplate.update("delete from smartbi_exchange.sb_data_freshness");
        jdbcTemplate.update("delete from smartbi_exchange.sb_dim_knowledge_point");
        jdbcTemplate.update("delete from smartbi_exchange.sb_dim_student");
        jdbcTemplate.update("delete from smartbi_exchange.sb_dim_class");
        jdbcTemplate.update("delete from smartbi_exchange.sb_dim_course");
        jdbcTemplate.update("delete from app.wrong_book_items");
        jdbcTemplate.update("delete from app.practice_attempts where data_origin = 'LIVE_DEMO'");
        jdbcTemplate.update("delete from app.demo_runs");
        jdbcTemplate.update("delete from app.learning_snapshot_history");
        jdbcTemplate.update("delete from app.coach_citations");
        jdbcTemplate.update("delete from app.coach_diagnostic_questions");
        jdbcTemplate.update("delete from app.coach_messages");
        jdbcTemplate.update("delete from app.coach_sessions");
        jdbcTemplate.update("delete from app.knowledge_chunks");
        jdbcTemplate.update("delete from app.knowledge_documents");
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates");
        jdbcTemplate.update("delete from app.student_learning_abilities");
        jdbcTemplate.update("delete from app.learning_snapshots");
        jdbcTemplate.update("delete from app.practice_questions");
        jdbcTemplate.update("delete from app.practice_attempts");
        jdbcTemplate.update("delete from app.practice_sets");
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
    void projectsCanonicalDimensionsAndFactsIdempotently() {
        AnalyticsProjectionResult first = projectionService.refresh();
        AnalyticsProjectionResult second = projectionService.refresh();

        assertThat(first.dimensionRows()).isEqualTo(93);
        assertThat(second.dimensionRows()).isEqualTo(first.dimensionRows());
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_dim_course", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_dim_class", Integer.class))
                .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_dim_student", Integer.class))
                .isEqualTo(80);
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_practice_attempt", Integer.class))
                .isEqualTo(jdbcTemplate.queryForObject("select count(*) from app.practice_attempts", Integer.class));
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_learning_state", Integer.class))
                .isEqualTo(jdbcTemplate.queryForObject("select count(*) from app.learning_snapshots", Integer.class));
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_diagnosis", Integer.class))
                .isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from app.domain_event_outbox", Integer.class))
                .isGreaterThan(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from app.domain_event_outbox where published_at is null", Integer.class))
                .isZero();
    }

    @Test
    void liveFactsCoexistAndResetMarksOldRunInactive() {
        DemoRun first = demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
        jdbcTemplate.update("""
                insert into app.practice_attempts
                    (attempt_id, practice_set_id, attempt_time, student_id, course_id, class_id,
                     knowledge_point_id, question_id, question_source, difficulty, correct, duration_seconds,
                     response_time_ms, attempt_index, selected_answer, data_origin, source_version,
                     baseline_version, demo_run_id, demo_case_id, correlation_id, ingested_at)
                values (?, null, ?, ?, ?, ?, ?, ?, 'LIVE_DIAGNOSTIC', 'HARD', false, 30, 30000, 99, 'B',
                        'LIVE_DEMO', 'live-v1', ?, ?, ?, ?, ?)
                """, "live-attempt-t05", OffsetDateTime.parse("2026-08-29T15:00:00Z"),
                BaselineSeedService.XIAOMING_ID, BaselineSeedService.COURSE_ID, BaselineSeedService.CLASS_ONE_ID,
                BaselineSeedService.BFS_DFS_ID, "live-q-t05", BaselineSeedService.BASELINE_VERSION, first.demoRunId(), BaselineSeedService.DEMO_CASE_ID,
                first.correlationId(), OffsetDateTime.parse("2026-08-29T15:00:00Z"));

        projectionService.refresh();
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_practice_attempt where data_origin = 'BASELINE_SIMULATED'", Integer.class))
                .isGreaterThan(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_practice_attempt where attempt_id = 'live-attempt-t05'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select is_active_demo from smartbi_exchange.sb_fact_practice_attempt where attempt_id = 'live-attempt-t05'", Boolean.class))
                .isTrue();

        demoRunService.reset(first.demoRunId());
        projectionService.refresh();
        assertThat(jdbcTemplate.queryForObject("select is_active_demo from smartbi_exchange.sb_fact_practice_attempt where attempt_id = 'live-attempt-t05'", Boolean.class))
                .isFalse();
        assertThat(jdbcTemplate.queryForObject("select count(*) from smartbi_exchange.sb_fact_practice_attempt where data_origin = 'BASELINE_SIMULATED'", Integer.class))
                .isGreaterThan(0);
    }

    @Test
    void exposesCatalogAndFreshnessThroughUnifiedEnvelope() throws Exception {
        projectionService.refresh();

        mockMvc.perform(get("/api/v1/analytics/smartbi/datasets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.datasets").isArray())
                .andExpect(jsonPath("$.data.datasets[?(@.datasetKey == 'sb_fact_learning_state')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/analytics/smartbi/freshness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[?(@.datasetKey == 'sb_fact_practice_attempt')]").isNotEmpty());
    }

    @Test
    void createsCanonicalCsvPackageWithIdempotentCommand() throws Exception {
        AnalyticsExport first = exportService.create("ACTIVE_DEMO", null, "t05-export");
        AnalyticsExport replay = exportService.create("ACTIVE_DEMO", null, "t05-export");

        assertThat(replay.exportId()).isEqualTo(first.exportId());
        assertThat(first.status()).isEqualTo("SUCCEEDED");
        assertThat(first.files()).hasSize(11);
        assertThat(first.files()).allSatisfy(path -> assertThat(Files.exists(java.nio.file.Path.of(path))).isTrue());
        assertThat(Files.readString(java.nio.file.Path.of(first.manifestPath()))).contains("sb_fact_learning_state");
        String diagnosisCsv = first.files().stream()
                .filter(path -> path.endsWith("sb_fact_diagnosis.csv"))
                .findFirst()
                .orElseThrow();
        assertThat(Files.readAllLines(java.nio.file.Path.of(diagnosisCsv))).isNotEmpty();
    }
}
