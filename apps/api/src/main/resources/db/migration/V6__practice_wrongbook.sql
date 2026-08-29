CREATE TABLE IF NOT EXISTS app.practice_sets (
    practice_set_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id) ON DELETE CASCADE,
    coach_session_id VARCHAR(160),
    source VARCHAR(64) NOT NULL CHECK (source IN ('AI_COACH_DIAGNOSTIC', 'AI_COACH_SIMILAR', 'TEACHER_ASSIGNMENT')),
    status VARCHAR(32) NOT NULL CHECK (status IN ('OPEN', 'COMPLETED')),
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS app.practice_questions (
    practice_set_id VARCHAR(160) NOT NULL REFERENCES app.practice_sets(practice_set_id) ON DELETE CASCADE,
    question_id VARCHAR(160) NOT NULL,
    parent_question_id VARCHAR(160),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id) ON DELETE CASCADE,
    question_type VARCHAR(32) NOT NULL CHECK (question_type = 'SINGLE_CHOICE'),
    stem TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_answer VARCHAR(128) NOT NULL,
    explanation TEXT NOT NULL,
    diagnostic_target TEXT,
    difficulty NUMERIC(5,4) NOT NULL CHECK (difficulty >= 0 AND difficulty <= 1),
    generation_reason VARCHAR(64),
    model_provider VARCHAR(128),
    model_version VARCHAR(128),
    prompt_version VARCHAR(128),
    citations TEXT,
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (practice_set_id, question_id)
);

ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS practice_set_id VARCHAR(160) REFERENCES app.practice_sets(practice_set_id) ON DELETE CASCADE;
ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS selected_answer TEXT;
ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(200);
ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS coach_session_id VARCHAR(160);
ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS assignment_id VARCHAR(160);
ALTER TABLE app.practice_attempts
    ADD CONSTRAINT uq_practice_attempt_idempotency UNIQUE (student_id, question_id, idempotency_key);

CREATE TABLE IF NOT EXISTS app.practice_outcomes (
    outcome_id VARCHAR(160) PRIMARY KEY,
    practice_set_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.practice_sets(practice_set_id) ON DELETE CASCADE,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    accuracy NUMERIC(5,4) NOT NULL CHECK (accuracy >= 0 AND accuracy <= 1),
    attempt_count INTEGER NOT NULL CHECK (attempt_count >= 0),
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS app.wrong_book_items (
    wrong_item_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id) ON DELETE CASCADE,
    question_id VARCHAR(160) NOT NULL,
    source_attempt_id VARCHAR(160) NOT NULL REFERENCES app.practice_attempts(attempt_id) ON DELETE CASCADE,
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id) ON DELETE CASCADE,
    reason TEXT,
    status VARCHAR(32) NOT NULL CHECK (status IN ('TO_REVIEW', 'LEARNING', 'MASTERED')),
    review_count INTEGER NOT NULL CHECK (review_count >= 0),
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    repaired_at TIMESTAMP WITH TIME ZONE,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    UNIQUE (student_id, source_attempt_id)
);

CREATE TABLE IF NOT EXISTS app.learning_snapshot_history (
    history_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id) ON DELETE CASCADE,
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id) ON DELETE CASCADE,
    mastery NUMERIC(5,4) NOT NULL CHECK (mastery >= 0 AND mastery <= 1),
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    forgetting_risk NUMERIC(5,4) NOT NULL CHECK (forgetting_risk >= 0 AND forgetting_risk <= 1),
    evidence_count INTEGER NOT NULL,
    last_evidence_at TIMESTAMP WITH TIME ZONE NOT NULL,
    mastery_model_version VARCHAR(64) NOT NULL,
    ability_model_version VARCHAR(64) NOT NULL,
    forgetting_model_version VARCHAR(64) NOT NULL,
    confidence_model_version VARCHAR(64) NOT NULL,
    theta NUMERIC(12,8),
    theta_uncertainty NUMERIC(12,8),
    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    source_version VARCHAR(64) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_practice_questions_set ON app.practice_questions (practice_set_id);
CREATE INDEX IF NOT EXISTS idx_practice_attempts_set ON app.practice_attempts (practice_set_id);
CREATE INDEX IF NOT EXISTS idx_wrong_book_student ON app.wrong_book_items (student_id, status, added_at);
CREATE INDEX IF NOT EXISTS idx_learning_snapshot_history_scope
    ON app.learning_snapshot_history (student_id, course_id, knowledge_point_id, captured_at);
