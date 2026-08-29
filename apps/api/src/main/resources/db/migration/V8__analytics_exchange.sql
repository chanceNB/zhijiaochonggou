-- T05 publishes app-owned facts to a stable, read-only SmartBI contract.
-- The tables are projection storage; they do not own or recompute business state.

CREATE TABLE IF NOT EXISTS app.domain_event_outbox (
    event_id VARCHAR(200) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(200) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    payload TEXT,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_domain_event_outbox_pending
    ON app.domain_event_outbox (published_at, occurred_at, event_id);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_dim_course (
    course_id VARCHAR(128) PRIMARY KEY,
    course_name VARCHAR(200) NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_dim_class (
    class_id VARCHAR(128) PRIMARY KEY,
    class_name VARCHAR(200) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_dim_student (
    student_id VARCHAR(128) PRIMARY KEY,
    display_name VARCHAR(200) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_dim_knowledge_point (
    knowledge_point_id VARCHAR(128) PRIMARY KEY,
    display_name VARCHAR(200) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    parent_knowledge_point_id VARCHAR(128),
    sort_order INTEGER NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_learning_state (
    snapshot_id VARCHAR(220) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    knowledge_point_id VARCHAR(128) NOT NULL,
    mastery_probability NUMERIC(8,6) NOT NULL CHECK (mastery_probability >= 0 AND mastery_probability <= 1),
    confidence NUMERIC(8,6) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    forgetting_risk NUMERIC(8,6) NOT NULL CHECK (forgetting_risk >= 0 AND forgetting_risk <= 1),
    weakness_score NUMERIC(8,6) CHECK (weakness_score IS NULL OR (weakness_score >= 0 AND weakness_score <= 1)),
    evidence_count INTEGER NOT NULL CHECK (evidence_count > 0),
    last_evidence_at TIMESTAMP WITH TIME ZONE NOT NULL,
    mastery_model_version VARCHAR(64) NOT NULL,
    ability_model_version VARCHAR(64) NOT NULL,
    forgetting_model_version VARCHAR(64) NOT NULL,
    confidence_model_version VARCHAR(64) NOT NULL,
    snapshot_time TIMESTAMP WITH TIME ZONE NOT NULL,
    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    snapshot_status VARCHAR(16) NOT NULL CHECK (snapshot_status IN ('CURRENT', 'HISTORICAL')),
    is_current BOOLEAN NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_practice_attempt (
    attempt_id VARCHAR(160) PRIMARY KEY,
    practice_set_id VARCHAR(160),
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    question_id VARCHAR(160) NOT NULL,
    knowledge_point_id VARCHAR(128) NOT NULL,
    question_source VARCHAR(64) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    correct BOOLEAN NOT NULL,
    response_time_ms INTEGER NOT NULL CHECK (response_time_ms > 0),
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    attempt_time TIMESTAMP WITH TIME ZONE NOT NULL,
    attempt_index INTEGER NOT NULL CHECK (attempt_index > 0),
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active_demo BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_wrong_book (
    wrong_book_item_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    question_id VARCHAR(160) NOT NULL,
    knowledge_point_id VARCHAR(128) NOT NULL,
    source_attempt_id VARCHAR(160) NOT NULL,
    reason TEXT,
    status VARCHAR(32) NOT NULL,
    review_count INTEGER NOT NULL CHECK (review_count >= 0),
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    repaired_at TIMESTAMP WITH TIME ZONE,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active_demo BOOLEAN NOT NULL
);

-- Reserved contracts for future T07/T08 facts. They intentionally contain no rows in T05.
CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_diagnosis (
    event_id VARCHAR(200) PRIMARY KEY,
    case_id VARCHAR(160) NOT NULL,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128),
    knowledge_point_id VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    severity VARCHAR(32),
    confidence NUMERIC(8,6),
    hypothesis_code VARCHAR(128),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_intervention (
    event_id VARCHAR(200) PRIMARY KEY,
    intervention_id VARCHAR(160) NOT NULL,
    recommendation_id VARCHAR(160),
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128),
    knowledge_point_id VARCHAR(128),
    strategy_code VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    predicted_lift NUMERIC(8,6),
    prediction_low NUMERIC(8,6),
    prediction_high NUMERIC(8,6),
    assignment_id VARCHAR(160),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_intervention_outcome (
    event_id VARCHAR(200) PRIMARY KEY,
    intervention_id VARCHAR(160) NOT NULL,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128),
    knowledge_point_id VARCHAR(128),
    transfer_validation VARCHAR(64),
    actual_lift NUMERIC(8,6),
    prediction_deviation NUMERIC(8,6),
    practice_accuracy_after NUMERIC(8,6),
    mastery_after NUMERIC(8,6),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_data_freshness (
    dataset_key VARCHAR(160) PRIMARY KEY,
    latest_source_event_time TIMESTAMP WITH TIME ZONE,
    latest_projection_time TIMESTAMP WITH TIME ZONE NOT NULL,
    observed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    row_count BIGINT NOT NULL CHECK (row_count >= 0),
    source_version VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_demo_run_state (
    demo_run_id VARCHAR(128) PRIMARY KEY,
    demo_case_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    reset_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL,
    correlation_id VARCHAR(128) NOT NULL,
    source_version VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sb_learning_state_scope
    ON smartbi_exchange.sb_fact_learning_state (student_id, course_id, knowledge_point_id, is_current);
CREATE INDEX IF NOT EXISTS idx_sb_attempt_scope
    ON smartbi_exchange.sb_fact_practice_attempt (student_id, course_id, attempt_time);
CREATE INDEX IF NOT EXISTS idx_sb_wrong_book_scope
    ON smartbi_exchange.sb_fact_wrong_book (student_id, added_at, is_active_demo);
