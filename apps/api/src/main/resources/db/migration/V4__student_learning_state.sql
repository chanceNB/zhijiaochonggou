-- T02 owns algorithm-derived learning state. T01 raw facts remain unchanged.
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS evidence_count INTEGER;
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS last_evidence_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS mastery_model_version VARCHAR(64);
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS ability_model_version VARCHAR(64);
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS forgetting_model_version VARCHAR(64);
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS confidence_model_version VARCHAR(64);
ALTER TABLE app.learning_snapshots
    ADD COLUMN IF NOT EXISTS computed_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE IF NOT EXISTS app.student_learning_abilities (
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    theta NUMERIC(12,8) NOT NULL,
    theta_uncertainty NUMERIC(12,8) NOT NULL CHECK (theta_uncertainty > 0),
    ability_model_version VARCHAR(64) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (student_id, course_id)
);

CREATE TABLE IF NOT EXISTS app.weak_knowledge_point_candidates (
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    weakness_score NUMERIC(8,6) NOT NULL CHECK (weakness_score >= 0 AND weakness_score <= 1),
    confidence NUMERIC(8,6) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    evidence_count INTEGER NOT NULL CHECK (evidence_count > 0),
    rank_position INTEGER NOT NULL CHECK (rank_position > 0),
    reason_codes VARCHAR(512) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (student_id, course_id, knowledge_point_id)
);

ALTER TABLE app.learning_snapshots
    ALTER COLUMN evidence_count SET NOT NULL;
ALTER TABLE app.learning_snapshots
    ALTER COLUMN last_evidence_at SET NOT NULL;
ALTER TABLE app.learning_snapshots
    ALTER COLUMN mastery_model_version SET NOT NULL;
ALTER TABLE app.learning_snapshots
    ALTER COLUMN ability_model_version SET NOT NULL;
ALTER TABLE app.learning_snapshots
    ALTER COLUMN forgetting_model_version SET NOT NULL;
ALTER TABLE app.learning_snapshots
    ALTER COLUMN confidence_model_version SET NOT NULL;
ALTER TABLE app.learning_snapshots
    ALTER COLUMN computed_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_learning_snapshots_student_course
    ON app.learning_snapshots (student_id, course_id);
CREATE INDEX IF NOT EXISTS idx_weak_candidates_student_rank
    ON app.weak_knowledge_point_candidates (student_id, course_id, rank_position);
