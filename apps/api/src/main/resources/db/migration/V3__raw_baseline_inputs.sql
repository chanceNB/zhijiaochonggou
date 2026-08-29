-- T01 keeps raw historical facts separate from T02 algorithm-derived state.
CREATE TABLE IF NOT EXISTS app.question_items (
    question_id VARCHAR(160) PRIMARY KEY,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    question_source VARCHAR(64) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    item_difficulty NUMERIC(5,4) NOT NULL CHECK (item_difficulty >= 0 AND item_difficulty <= 1),
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS response_time_ms INTEGER;
ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS attempt_index INTEGER;

UPDATE app.practice_attempts current_attempt
SET response_time_ms = current_attempt.duration_seconds * 1000
WHERE current_attempt.response_time_ms IS NULL;

UPDATE app.practice_attempts current_attempt
SET attempt_index = (
    SELECT COUNT(*)
    FROM app.practice_attempts previous_attempt
    WHERE previous_attempt.student_id = current_attempt.student_id
      AND previous_attempt.knowledge_point_id = current_attempt.knowledge_point_id
      AND (
          previous_attempt.attempt_time < current_attempt.attempt_time
          OR (
              previous_attempt.attempt_time = current_attempt.attempt_time
              AND previous_attempt.attempt_id <= current_attempt.attempt_id
          )
      )
)
WHERE current_attempt.attempt_index IS NULL;

ALTER TABLE app.practice_attempts
    ALTER COLUMN response_time_ms SET NOT NULL;
ALTER TABLE app.practice_attempts
    ALTER COLUMN attempt_index SET NOT NULL;
ALTER TABLE app.practice_attempts
    ADD CONSTRAINT chk_practice_attempt_response_time CHECK (response_time_ms > 0);
ALTER TABLE app.practice_attempts
    ADD CONSTRAINT chk_practice_attempt_index CHECK (attempt_index > 0);

-- Remove snapshots produced by the pre-correction seed; T02 owns their computation.
DELETE FROM app.learning_snapshots;

CREATE INDEX IF NOT EXISTS idx_question_items_knowledge_point
    ON app.question_items (knowledge_point_id);
