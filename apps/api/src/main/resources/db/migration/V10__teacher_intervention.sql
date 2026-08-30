-- T07 owns teacher-side recommendation capture and intervention lifecycle.
CREATE TABLE IF NOT EXISTS app.analysis_recommendations (
    recommendation_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id) ON DELETE CASCADE,
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id) ON DELETE CASCADE,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    analysis_summary TEXT NOT NULL,
    evidence_refs TEXT NOT NULL,
    source VARCHAR(64) NOT NULL CHECK (source = 'SMARTBI_AICHAT'),
    capture_mode VARCHAR(32) NOT NULL CHECK (capture_mode = 'MANUAL'),
    status VARCHAR(32) NOT NULL CHECK (status = 'PENDING_TEACHER_REVIEW'),
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS app.analysis_recommendation_candidates (
    recommendation_id VARCHAR(160) NOT NULL REFERENCES app.analysis_recommendations(recommendation_id) ON DELETE CASCADE,
    candidate_index INTEGER NOT NULL CHECK (candidate_index BETWEEN 1 AND 3),
    strategy_code VARCHAR(128) NOT NULL,
    title VARCHAR(256) NOT NULL,
    rationale TEXT NOT NULL,
    action_description TEXT NOT NULL,
    source_snapshot TEXT NOT NULL,
    PRIMARY KEY (recommendation_id, candidate_index),
    UNIQUE (recommendation_id, strategy_code)
);

CREATE TABLE IF NOT EXISTS app.interventions (
    intervention_id VARCHAR(160) PRIMARY KEY,
    recommendation_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.analysis_recommendations(recommendation_id),
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id) ON DELETE CASCADE,
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id) ON DELETE CASCADE,
    strategy_code VARCHAR(128) NOT NULL,
    teacher_rationale TEXT NOT NULL,
    predicted_lift NUMERIC(8,6) NOT NULL CHECK (predicted_lift >= 0 AND predicted_lift <= 1),
    prediction_low NUMERIC(8,6) NOT NULL CHECK (prediction_low >= 0 AND prediction_low <= 1),
    prediction_high NUMERIC(8,6) NOT NULL CHECK (prediction_high >= 0 AND prediction_high <= 1),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PROPOSED', 'APPROVED', 'COMMITTED')),
    version INTEGER NOT NULL CHECK (version > 0),
    assignment_id VARCHAR(160),
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(200) NOT NULL UNIQUE,
    approve_idempotency_key VARCHAR(200),
    commit_idempotency_key VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    approved_at TIMESTAMP WITH TIME ZONE,
    committed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS app.intervention_assignments (
    assignment_id VARCHAR(160) PRIMARY KEY,
    intervention_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.interventions(intervention_id) ON DELETE CASCADE,
    practice_set_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.practice_sets(practice_set_id),
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id) ON DELETE CASCADE,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id) ON DELETE CASCADE,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id) ON DELETE CASCADE,
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL CHECK (status = 'PENDING_STUDENT'),
    due_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_analysis_recommendations_student
    ON app.analysis_recommendations (student_id, course_id, captured_at);
CREATE INDEX IF NOT EXISTS idx_interventions_status
    ON app.interventions (status, created_at);
CREATE INDEX IF NOT EXISTS idx_intervention_assignments_student
    ON app.intervention_assignments (student_id, status, due_at);
