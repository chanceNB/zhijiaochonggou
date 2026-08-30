package com.zhijiao.foundation.teacher;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** B-owned deterministic cohort estimate; it never consumes a lift from AIChat. */
@Service
public class HistoricalEffectEstimator implements EffectEstimator {
    private final JdbcTemplate jdbcTemplate;

    public HistoricalEffectEstimator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public EffectEstimate estimate(AnalysisRecommendation recommendation, AnalysisRecommendation.Candidate candidate) {
        Long total = jdbcTemplate.queryForObject("""
                select count(*) from app.practice_attempts
                where course_id = ? and knowledge_point_id = ? and student_id <> ?
                """, Long.class, recommendation.courseId(), recommendation.knowledgePointId(), recommendation.studentId());
        Long correct = jdbcTemplate.queryForObject("""
                select count(*) from app.practice_attempts
                where course_id = ? and knowledge_point_id = ? and student_id <> ? and correct = true
                """, Long.class, recommendation.courseId(), recommendation.knowledgePointId(), recommendation.studentId());
        double cohortAccuracy = total == null || total == 0 ? 0.5 : (correct == null ? 0 : correct) / (double) total;
        double strategyPrior = switch (candidate.strategyCode().toUpperCase(Locale.ROOT)) {
            case "VISUAL_TRANSFER_PRACTICE" -> 0.11;
            case "AI_GUIDED_VARIATION" -> 0.09;
            default -> 0.08;
        };
        double lift = clamp(strategyPrior + 0.05 * (1.0 - cohortAccuracy), 0.02, 0.30);
        return new EffectEstimate(lift, Math.max(0.0, lift - 0.04), Math.min(1.0, lift + 0.04));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
