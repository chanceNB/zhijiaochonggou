package com.zhijiao.foundation.student.practice;

import com.zhijiao.foundation.teacher.InterventionAssignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/** Copies existing structured practice facts into a committed teacher assignment. */
@Service
public class TeacherAssignmentProvisioner {
    private final JdbcTemplate jdbcTemplate;

    public TeacherAssignmentProvisioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void ensureProvisioned(InterventionAssignment assignment) {
        Integer existing = jdbcTemplate.queryForObject(
                "select count(*) from app.practice_questions where practice_set_id = ?", Integer.class,
                assignment.practiceSetId());
        int existingCount = existing == null ? 0 : existing;
        if (existingCount >= 2) return;

        List<Map<String, Object>> source = jdbcTemplate.queryForList("""
                select q.question_id, q.parent_question_id, q.knowledge_point_id, q.question_type,
                       q.stem, q.options, q.correct_answer, q.explanation, q.difficulty,
                       q.source_version, q.created_at
                from app.practice_questions q
                join app.practice_sets s on s.practice_set_id = q.practice_set_id
                where q.knowledge_point_id = ?
                  and q.practice_set_id <> ?
                  and s.source <> 'TEACHER_ASSIGNMENT'
                order by q.created_at, q.question_id
                limit 2
                """, assignment.knowledgePointId(), assignment.practiceSetId());
        if (source.size() < 2) {
            throw new DomainRuleViolationException("Teacher assignment requires two existing structured practice questions");
        }

        for (int index = existingCount; index < 2; index++) {
            Map<String, Object> row = source.get(index);
            String targetQuestionId = assignment.assignmentId() + "-q-" + (index + 1);
            jdbcTemplate.update("""
                    insert into app.practice_questions
                        (practice_set_id, question_id, parent_question_id, knowledge_point_id, question_type,
                         stem, options, correct_answer, explanation, difficulty, validation_role,
                         generation_reason, source_version, created_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'TEACHER_ASSIGNMENT_PROVISIONED', ?, ?)
                    """, assignment.practiceSetId(), targetQuestionId, row.get("parent_question_id"),
                    row.get("knowledge_point_id"), row.get("question_type"), row.get("stem"), row.get("options"),
                    row.get("correct_answer"), row.get("explanation"), row.get("difficulty"), index == 1 ? "TRANSFER" : "DIAGNOSTIC",
                    row.get("source_version"), timestamp(row.get("created_at")));
        }
    }

    private Object timestamp(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant().atOffset(ZoneOffset.UTC);
        if (value instanceof java.time.Instant instant) return instant.atOffset(ZoneOffset.UTC);
        if (value == null) return null;
        throw new IllegalArgumentException("Unsupported source question timestamp: " + value);
    }
}
