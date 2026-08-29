CREATE TABLE IF NOT EXISTS app.baseline_metadata (
    baseline_version VARCHAR(64) PRIMARY KEY,
    reference_date DATE NOT NULL,
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS app.courses (
    course_id VARCHAR(128) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS app.classrooms (
    class_id VARCHAR(128) PRIMARY KEY,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    name VARCHAR(200) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS app.students (
    student_id VARCHAR(128) PRIMARY KEY,
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    display_name VARCHAR(200) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS app.knowledge_points (
    knowledge_point_id VARCHAR(128) PRIMARY KEY,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    parent_id VARCHAR(128) REFERENCES app.knowledge_points(knowledge_point_id),
    name VARCHAR(200) NOT NULL,
    sort_order INTEGER NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS app.learning_snapshots (
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    mastery NUMERIC(5,4) NOT NULL CHECK (mastery >= 0 AND mastery <= 1),
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    forgetting_risk NUMERIC(5,4) NOT NULL CHECK (forgetting_risk >= 0 AND forgetting_risk <= 1),
    misconception_code VARCHAR(128),
    snapshot_time TIMESTAMP WITH TIME ZONE NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    data_origin VARCHAR(32) NOT NULL CHECK (data_origin = 'BASELINE_SIMULATED'),
    source_version VARCHAR(64) NOT NULL,
    PRIMARY KEY (student_id, knowledge_point_id)
);

CREATE TABLE IF NOT EXISTS app.learning_events (
    event_id VARCHAR(160) PRIMARY KEY,
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    event_type VARCHAR(64) NOT NULL,
    correct BOOLEAN,
    mastery_after NUMERIC(5,4) CHECK (mastery_after IS NULL OR (mastery_after >= 0 AND mastery_after <= 1)),
    confidence NUMERIC(5,4) CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 1)),
    forgetting_risk NUMERIC(5,4) CHECK (forgetting_risk IS NULL OR (forgetting_risk >= 0 AND forgetting_risk <= 1)),
    misconception_code VARCHAR(128),
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK ((data_origin = 'BASELINE_SIMULATED' AND demo_run_id IS NULL)
        OR (data_origin = 'LIVE_DEMO' AND demo_run_id IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS app.practice_attempts (
    attempt_id VARCHAR(160) PRIMARY KEY,
    attempt_time TIMESTAMP WITH TIME ZONE NOT NULL,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    question_id VARCHAR(160) NOT NULL,
    question_source VARCHAR(64) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    correct BOOLEAN NOT NULL,
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    misconception_code VARCHAR(128),
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK ((data_origin = 'BASELINE_SIMULATED' AND demo_run_id IS NULL)
        OR (data_origin = 'LIVE_DEMO' AND demo_run_id IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS app.demo_runs (
    demo_run_id VARCHAR(128) PRIMARY KEY,
    demo_case_id VARCHAR(128) NOT NULL,
    baseline_version VARCHAR(64) NOT NULL REFERENCES app.baseline_metadata(baseline_version),
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'RESET')),
    stage VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reset_at TIMESTAMP WITH TIME ZONE,
    reset_from_demo_run_id VARCHAR(128) REFERENCES app.demo_runs(demo_run_id)
);

CREATE INDEX IF NOT EXISTS idx_learning_events_student_time
    ON app.learning_events (student_id, event_time);
CREATE INDEX IF NOT EXISTS idx_learning_events_origin_run
    ON app.learning_events (data_origin, demo_run_id);
CREATE INDEX IF NOT EXISTS idx_practice_attempts_student_time
    ON app.practice_attempts (student_id, attempt_time);
CREATE INDEX IF NOT EXISTS idx_practice_attempts_origin_run
    ON app.practice_attempts (data_origin, demo_run_id);
CREATE INDEX IF NOT EXISTS idx_demo_runs_case_status
    ON app.demo_runs (demo_case_id, status);
