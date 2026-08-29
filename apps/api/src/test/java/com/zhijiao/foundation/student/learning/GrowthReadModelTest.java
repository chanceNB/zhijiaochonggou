package com.zhijiao.foundation.student.learning;

import com.zhijiao.foundation.demo.BaselineSeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GrowthReadModelTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private LearningStateEngine learningStateEngine;

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
    void growthReadModelAggregatesCurrentDerivedStateWithoutInterventionFacts() {
        GrowthReadModel growth = learningStateEngine.readGrowth(
                "stu-xiaoming", "course-data-structures");

        assertThat(growth.mastery()).isBetween(0.0, 1.0);
        assertThat(growth.trend()).isNotEmpty();
        assertThat(growth.completedTasks()).isZero();
        assertThat(growth.repairedMisconceptions()).isZero();
        assertThat(growth.latestIntervention()).isNull();
    }
}
