package com.zhijiao.foundation.student.learning;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Enriches the A-owned growth state with B-owned intervention outcome facts. */
@Service
public class GrowthReadModelService {
    private final LearningStateEngine learningStateEngine;
    private final JdbcTemplate jdbcTemplate;

    public GrowthReadModelService(LearningStateEngine learningStateEngine, JdbcTemplate jdbcTemplate) {
        this.learningStateEngine = learningStateEngine;
        this.jdbcTemplate = jdbcTemplate;
    }

    public GrowthReadModel read(String studentId, String courseId) {
        GrowthReadModel base = learningStateEngine.readGrowth(studentId, courseId);
        Optional<GrowthReadModel.LatestIntervention> latest = jdbcTemplate.query("""
                select i.strategy_code, o.mastery_before, o.mastery_after, o.transfer_validation,
                       o.predicted_lift, o.actual_lift, o.prediction_deviation,
                       o.confidence_before, o.confidence_after, o.forgetting_risk_before,
                       o.forgetting_risk_after, o.evidence_count_before, o.evidence_count_after
                from app.intervention_outcomes o
                join app.interventions i on i.intervention_id = o.intervention_id
                where o.student_id = ? and o.course_id = ?
                order by o.completed_at desc, o.outcome_id desc limit 1
                """, (rs, rowNum) -> new GrowthReadModel.LatestIntervention(
                rs.getString("strategy_code"), rs.getDouble("mastery_before"), rs.getDouble("mastery_after"),
                rs.getString("transfer_validation"), rs.getDouble("predicted_lift"), rs.getDouble("actual_lift"),
                rs.getDouble("prediction_deviation"), rs.getDouble("confidence_before"), rs.getDouble("confidence_after"),
                rs.getDouble("forgetting_risk_before"), rs.getDouble("forgetting_risk_after"),
                rs.getInt("evidence_count_before"), rs.getInt("evidence_count_after")), studentId, courseId).stream().findFirst();
        return new GrowthReadModel(base.studentId(), base.courseId(), base.mastery(), base.trend(), base.completedTasks(),
                base.repairedMisconceptions(), latest.orElse(null));
    }
}
