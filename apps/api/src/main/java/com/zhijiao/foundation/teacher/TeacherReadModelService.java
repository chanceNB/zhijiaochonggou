package com.zhijiao.foundation.teacher;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Read-only teacher projections composed from the authoritative application tables. */
@Service
public class TeacherReadModelService {
    private final JdbcTemplate jdbc;

    public TeacherReadModelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public WorkbenchResponse workbench() {
        CurrentStudent student = findCurrentStudent().orElse(null);
        if (student == null) return new WorkbenchResponse(null, List.of(), List.of(), List.of());
        return new WorkbenchResponse(toContext(student), findPriority(student), findPendingRecommendations(student),
                findPendingOutcomes(student));
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile(String studentId, String courseId) {
        CurrentStudent student = findStudent(studentId, courseId)
                .orElseThrow(() -> new TeacherReadModelNotFoundException("student", studentId));
        return new ProfileResponse(toStudent(student), findLearningState(student).orElse(null), findAttempts(student),
                findDiagnosis(student).orElse(null), findIntervention(student).orElse(null));
    }

    @Transactional(readOnly = true)
    public DiagnosisResponse diagnosis(String caseId) {
        CurrentStudent student = jdbc.query("""
                select d.demo_case_id, d.student_id, d.course_id, d.class_id, s.display_name, c.name course_name,
                       cl.name class_name
                from app.demo_runs d
                join app.students s on s.student_id = d.student_id
                join app.courses c on c.course_id = d.course_id
                join app.classrooms cl on cl.class_id = d.class_id
                where d.demo_case_id = ? and d.status = 'ACTIVE'
                order by d.created_at desc limit 1
                """, (rs, row) -> new CurrentStudent(rs.getString("student_id"), rs.getString("display_name"),
                        rs.getString("course_id"), rs.getString("course_name"), rs.getString("class_id"),
                        rs.getString("class_name"), rs.getString("demo_case_id")), caseId).stream().findFirst()
                .orElseThrow(() -> new TeacherReadModelNotFoundException("diagnosis case", caseId));
        DiagnosisSummary summary = findDiagnosis(student)
                .orElseThrow(() -> new TeacherReadModelNotFoundException("diagnosis evidence", caseId));
        return new DiagnosisResponse(caseId, summary.severity(), summary.confidence(), summary.primaryHypothesis(),
                summary.evidence(), summary.counterEvidence(), student.displayName(), student.courseName(),
                student.className());
    }

    private Optional<CurrentStudent> findCurrentStudent() {
        return jdbc.query("""
                select d.student_id, s.display_name, d.course_id, c.name course_name, d.class_id, cl.name class_name,
                       d.demo_case_id
                from app.demo_runs d
                join app.students s on s.student_id = d.student_id
                join app.courses c on c.course_id = d.course_id
                join app.classrooms cl on cl.class_id = d.class_id
                where d.status = 'ACTIVE'
                order by d.created_at desc limit 1
                """, (rs, row) -> new CurrentStudent(rs.getString("student_id"), rs.getString("display_name"),
                        rs.getString("course_id"), rs.getString("course_name"), rs.getString("class_id"),
                        rs.getString("class_name"), rs.getString("demo_case_id"))).stream().findFirst();
    }

    private Optional<CurrentStudent> findStudent(String studentId, String courseId) {
        return jdbc.query("""
                select s.student_id, s.display_name, s.course_id, c.name course_name, s.class_id, cl.name class_name,
                       d.demo_case_id
                from app.students s
                join app.courses c on c.course_id = s.course_id
                join app.classrooms cl on cl.class_id = s.class_id
                left join app.demo_runs d on d.student_id = s.student_id and d.course_id = s.course_id
                    and d.status = 'ACTIVE'
                where s.student_id = ? and s.course_id = ?
                order by d.created_at desc nulls last limit 1
                """, (rs, row) -> new CurrentStudent(rs.getString("student_id"), rs.getString("display_name"),
                        rs.getString("course_id"), rs.getString("course_name"), rs.getString("class_id"),
                        rs.getString("class_name"), rs.getString("demo_case_id")), studentId, courseId).stream().findFirst();
    }

    private StudentContext toContext(CurrentStudent s) {
        return new StudentContext(s.studentId(), s.displayName(), s.courseId(), s.courseName(), s.classId(), s.className(), s.demoCaseId());
    }

    private StudentProfile toStudent(CurrentStudent s) {
        return new StudentProfile(s.studentId(), s.displayName(), s.courseId(), s.courseName(), s.classId(), s.className());
    }

    private List<PriorityItem> findPriority(CurrentStudent s) {
        List<PriorityItem> result = new ArrayList<>();
        jdbc.query("""
                select a.assignment_id, a.status, k.name knowledge_point_name, i.strategy_code
                from app.intervention_assignments a
                join app.interventions i on i.intervention_id = a.intervention_id
                join app.knowledge_points k on k.knowledge_point_id = a.knowledge_point_id
                where a.student_id = ? and a.course_id = ? and a.status in ('PENDING_STUDENT','IN_PROGRESS')
                  and i.status = 'COMMITTED'
                order by a.created_at desc limit 1
                """, (rs, row) -> new PriorityItem("TEACHER_ASSIGNMENT", "教师任务待完成",
                        "围绕「" + rs.getString("knowledge_point_name") + "」的真实练习任务。", rs.getString("status"),
                        rs.getString("knowledge_point_name"), rs.getString("strategy_code")), s.studentId(), s.courseId())
                .forEach(result::add);
        jdbc.query("""
                select k.name knowledge_point_name, ls.mastery, c.confidence, c.evidence_count, c.reason_codes
                from app.weak_knowledge_point_candidates c
                join app.knowledge_points k on k.knowledge_point_id = c.knowledge_point_id
                join app.learning_snapshots ls on ls.student_id = c.student_id
                    and ls.course_id = c.course_id and ls.knowledge_point_id = c.knowledge_point_id
                where c.student_id = ? and c.course_id = ?
                order by c.rank_position limit 1
                """, (rs, row) -> new PriorityItem("LEARNING_ISSUE", "当前学习问题",
                        "「" + rs.getString("knowledge_point_name") + "」的学习证据需要教师关注。掌握度 "
                                + percent(rs.getDouble("mastery")) + "%，证据 " + rs.getInt("evidence_count") + " 条。",
                        "AVAILABLE", rs.getString("knowledge_point_name"), rs.getString("reason_codes")), s.studentId(), s.courseId())
                .forEach(result::add);
        return result;
    }

    private List<PendingRecommendation> findPendingRecommendations(CurrentStudent s) {
        return jdbc.query("""
                select r.recommendation_id, r.analysis_summary, r.status, k.name knowledge_point_name, r.captured_at
                from app.analysis_recommendations r
                join app.knowledge_points k on k.knowledge_point_id = r.knowledge_point_id
                where r.student_id = ? and r.course_id = ? and r.status = 'PENDING_TEACHER_REVIEW'
                order by r.captured_at desc
                """, (rs, row) -> new PendingRecommendation(rs.getString("recommendation_id"),
                rs.getString("analysis_summary"), rs.getString("status"), rs.getString("knowledge_point_name"),
                instant(rs.getObject("captured_at"))), s.studentId(), s.courseId());
    }

    private List<PendingOutcome> findPendingOutcomes(CurrentStudent s) {
        return jdbc.query("""
                select i.intervention_id, i.strategy_code, i.status, k.name knowledge_point_name, i.committed_at
                from app.interventions i
                join app.knowledge_points k on k.knowledge_point_id = i.knowledge_point_id
                left join app.intervention_outcomes o on o.intervention_id = i.intervention_id
                where i.student_id = ? and i.course_id = ? and i.status = 'COMMITTED' and o.outcome_id is null
                order by i.committed_at desc
                """, (rs, row) -> new PendingOutcome(rs.getString("intervention_id"), rs.getString("strategy_code"),
                rs.getString("status"), rs.getString("knowledge_point_name"), instant(rs.getObject("committed_at"))),
                s.studentId(), s.courseId());
    }

    private Optional<LearningState> findLearningState(CurrentStudent s) {
        return jdbc.query("""
                select k.name knowledge_point_name, ls.mastery, ls.confidence, ls.forgetting_risk, ls.evidence_count,
                       c.weakness_score, c.reason_codes, ls.computed_at
                from app.learning_snapshots ls
                join app.knowledge_points k on k.knowledge_point_id = ls.knowledge_point_id
                left join app.weak_knowledge_point_candidates c on c.student_id = ls.student_id
                    and c.course_id = ls.course_id and c.knowledge_point_id = ls.knowledge_point_id
                where ls.student_id = ? and ls.course_id = ?
                order by coalesce(c.rank_position, 999999), ls.computed_at desc limit 1
                """, (rs, row) -> new LearningState(rs.getString("knowledge_point_name"), rs.getDouble("mastery"),
                rs.getDouble("confidence"), rs.getDouble("forgetting_risk"), rs.getInt("evidence_count"),
                nullableDouble(rs, "weakness_score"), rs.getString("reason_codes"), instant(rs.getObject("computed_at"))),
                s.studentId(), s.courseId()).stream().findFirst();
    }

    private List<RecentAttempt> findAttempts(CurrentStudent s) {
        return jdbc.query("""
                select p.attempt_id, p.question_id, p.correct, p.duration_seconds, p.attempt_time,
                       k.name knowledge_point_name, coalesce(nullif(pq.stem, ''), '练习题') question_summary,
                       p.misconception_code
                from app.practice_attempts p
                join app.knowledge_points k on k.knowledge_point_id = p.knowledge_point_id
                left join app.practice_questions pq on pq.practice_set_id = p.practice_set_id and pq.question_id = p.question_id
                where p.student_id = ? and p.course_id = ?
                order by p.attempt_time desc, p.attempt_id desc limit 8
                """, (rs, row) -> new RecentAttempt(rs.getString("question_id"), rs.getString("knowledge_point_name"),
                rs.getString("question_summary"), rs.getBoolean("correct"), rs.getInt("duration_seconds"),
                instant(rs.getObject("attempt_time")), rs.getString("misconception_code")), s.studentId(), s.courseId());
    }

    private Optional<DiagnosisSummary> findDiagnosis(CurrentStudent s) {
        Optional<LearningState> state = findLearningState(s);
        if (state.isEmpty()) return Optional.empty();
        LearningState learning = state.get();
        List<RecentAttempt> attempts = findAttempts(s);
        if (attempts.isEmpty()) return Optional.empty();
        List<String> evidence = new ArrayList<>();
        evidence.add("知识点「" + learning.knowledgePointName() + "」有 " + learning.evidenceCount() + " 条学习证据。");
        evidence.add("当前掌握度 " + percent(learning.mastery()) + "%，遗忘风险 " + percent(learning.forgettingRisk()) + "%。");
        attempts.stream().limit(3).forEach(a -> evidence.add((a.correct() ? "最近一次答题正确" : "最近一次答题未答对")
                + "，发生于 " + a.attemptTime() + "。"));
        List<String> counter = attempts.stream().filter(RecentAttempt::correct).limit(2)
                .map(a -> "存在答对的练习记录（" + a.attemptTime() + "）。").toList();
        return Optional.of(new DiagnosisSummary(
                severity(learning.weaknessScore() == null ? 0.0 : learning.weaknessScore()), learning.confidence(),
                "知识点「" + learning.knowledgePointName() + "」仍需要巩固与迁移验证", evidence, counter));
    }

    private Optional<InterventionSummary> findIntervention(CurrentStudent s) {
        return jdbc.query("""
                select i.strategy_code, i.status, i.teacher_rationale, a.status assignment_status,
                       o.transfer_validation, o.practice_accuracy_after
                from app.interventions i
                left join app.intervention_assignments a on a.intervention_id = i.intervention_id
                left join app.intervention_outcomes o on o.intervention_id = i.intervention_id
                where i.student_id = ? and i.course_id = ?
                order by i.created_at desc limit 1
                """, (rs, row) -> new InterventionSummary(strategyLabel(rs.getString("strategy_code")), statusLabel(rs.getString("status")),
                rs.getString("teacher_rationale"), statusLabel(rs.getString("assignment_status")), statusLabel(rs.getString("transfer_validation")),
                nullableDouble(rs, "practice_accuracy_after")), s.studentId(), s.courseId()).stream().findFirst();
    }

    private static String severity(double weakness) {
        return weakness >= .75 ? "HIGH" : weakness >= .5 ? "MEDIUM" : "LOW";
    }

    private static String strategyLabel(String value) {
        return switch (value) {
            case "VISUAL_TRANSFER_PRACTICE" -> "可视化迁移练习";
            case "CONCEPT_REMEDIATION" -> "概念补强";
            case "AI_GUIDED_VARIATION" -> "AI 引导变式练习";
            default -> value;
        };
    }

    private static String statusLabel(String value) {
        if (value == null) return null;
        return switch (value) {
            case "PENDING_STUDENT" -> "待完成";
            case "IN_PROGRESS" -> "进行中";
            case "COMPLETED" -> "已完成";
            case "PROPOSED" -> "待确认";
            case "APPROVED" -> "已确认";
            case "COMMITTED" -> "已提交";
            case "PASS" -> "已通过";
            case "FAIL" -> "未通过";
            case "NOT_RUN" -> "未执行";
            default -> value;
        };
    }

    private static String percent(double value) { return Math.round(value * 100) + ""; }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Instant instant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }

    private record CurrentStudent(String studentId, String displayName, String courseId, String courseName,
                                  String classId, String className, String demoCaseId) {}

    public record WorkbenchResponse(StudentContext currentStudent, List<PriorityItem> priorityItems,
                                    List<PendingRecommendation> pendingRecommendations,
                                    List<PendingOutcome> pendingOutcomes) {}
    public record StudentContext(String studentId, String displayName, String courseId, String courseName,
                                 String classId, String className, String demoCaseId) {}
    public record PriorityItem(String type, String title, String description, String status,
                               String knowledgePointName, String strategy) {}
    public record PendingRecommendation(String recommendationId, String summary, String status,
                                        String knowledgePointName, Instant capturedAt) {}
    public record PendingOutcome(String interventionId, String strategy, String status,
                                 String knowledgePointName, Instant committedAt) {}
    public record ProfileResponse(StudentProfile student, LearningState learningState,
                                  List<RecentAttempt> recentAttempts, DiagnosisSummary diagnosis,
                                  InterventionSummary intervention) {}
    public record StudentProfile(String studentId, String displayName, String courseId, String courseName,
                                 String classId, String className) {}
    public record LearningState(String knowledgePointName, double mastery, double confidence,
                                double forgettingRisk, int evidenceCount, Double weaknessScore,
                                String reasonCodes, Instant computedAt) {}
    public record RecentAttempt(String questionId, String knowledgePointName, String questionSummary,
                                boolean correct, int durationSeconds, Instant attemptTime, String misconceptionCode) {}
    public record DiagnosisSummary(String severity, double confidence, String primaryHypothesis,
                                   List<String> evidence, List<String> counterEvidence) {}
    public record InterventionSummary(String strategy, String status, String teacherRationale,
                                      String assignmentStatus, String transferValidation, Double practiceAccuracyAfter) {}
    public record DiagnosisResponse(String caseId, String severity, double confidence, String primaryHypothesis,
                                    List<String> evidence, List<String> counterEvidence, String studentName,
                                    String courseName, String className) {}
}
