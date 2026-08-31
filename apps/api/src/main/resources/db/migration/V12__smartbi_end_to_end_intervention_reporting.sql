-- F04 pre-SmartBI reporting contract. app.* remains the business-fact owner.
-- These tables are rebuilt by AnalyticsProjectionService and are read-only exchange facts.

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_analysis_recommendation (
    recommendation_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    knowledge_point_id VARCHAR(128) NOT NULL,
    analysis_summary TEXT NOT NULL,
    evidence_refs TEXT NOT NULL,
    source VARCHAR(64) NOT NULL CHECK (source = 'SMARTBI_AICHAT'),
    capture_mode VARCHAR(32) NOT NULL CHECK (capture_mode = 'MANUAL'),
    status VARCHAR(32) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active_demo BOOLEAN NOT NULL,
    is_active_demo_flag SMALLINT NOT NULL CHECK (is_active_demo_flag IN (0, 1))
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_analysis_recommendation_candidate (
    recommendation_id VARCHAR(160) NOT NULL,
    candidate_index INTEGER NOT NULL CHECK (candidate_index BETWEEN 1 AND 3),
    strategy_code VARCHAR(128) NOT NULL,
    title VARCHAR(256) NOT NULL,
    rationale TEXT NOT NULL,
    action_description TEXT NOT NULL,
    source_snapshot TEXT,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    knowledge_point_id VARCHAR(128) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active_demo BOOLEAN NOT NULL,
    is_active_demo_flag SMALLINT NOT NULL CHECK (is_active_demo_flag IN (0, 1)),
    PRIMARY KEY (recommendation_id, candidate_index),
    UNIQUE (recommendation_id, strategy_code)
);

CREATE TABLE IF NOT EXISTS smartbi_exchange.sb_fact_intervention_assignment (
    assignment_id VARCHAR(160) PRIMARY KEY,
    intervention_id VARCHAR(160) NOT NULL,
    practice_set_id VARCHAR(160) NOT NULL,
    student_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    class_id VARCHAR(128) NOT NULL,
    knowledge_point_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING_STUDENT', 'IN_PROGRESS', 'COMPLETED')),
    due_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active_demo BOOLEAN NOT NULL,
    is_active_demo_flag SMALLINT NOT NULL CHECK (is_active_demo_flag IN (0, 1))
);

CREATE INDEX IF NOT EXISTS idx_sb_recommendation_scope
    ON smartbi_exchange.sb_fact_analysis_recommendation (student_id, captured_at, is_active_demo);
CREATE INDEX IF NOT EXISTS idx_sb_candidate_recommendation
    ON smartbi_exchange.sb_fact_analysis_recommendation_candidate (recommendation_id, candidate_index);
CREATE INDEX IF NOT EXISTS idx_sb_assignment_scope
    ON smartbi_exchange.sb_fact_intervention_assignment (student_id, created_at, status, is_active_demo);

ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS teacher_rationale TEXT;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS mastery_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS confidence_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS forgetting_risk_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS weakness_score_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS evidence_count_before INTEGER;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS before_captured_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS is_active_demo BOOLEAN;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD COLUMN IF NOT EXISTS is_active_demo_flag SMALLINT;
UPDATE smartbi_exchange.sb_fact_intervention
   SET teacher_rationale = COALESCE(teacher_rationale, ''),
       is_active_demo = CASE WHEN data_origin <> 'LIVE_DEMO' THEN TRUE ELSE EXISTS (
           SELECT 1 FROM app.demo_runs d WHERE d.demo_run_id = sb_fact_intervention.demo_run_id AND d.status = 'ACTIVE'
       ) END,
       is_active_demo_flag = CASE WHEN data_origin <> 'LIVE_DEMO' THEN 1 ELSE CASE WHEN EXISTS (
           SELECT 1 FROM app.demo_runs d WHERE d.demo_run_id = sb_fact_intervention.demo_run_id AND d.status = 'ACTIVE'
       ) THEN 1 ELSE 0 END END
 WHERE teacher_rationale IS NULL OR is_active_demo IS NULL OR is_active_demo_flag IS NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ALTER COLUMN teacher_rationale SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ALTER COLUMN is_active_demo SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ALTER COLUMN is_active_demo_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention
    ADD CONSTRAINT chk_sb_fact_intervention_is_active_demo_flag CHECK (is_active_demo_flag IN (0, 1));

ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS is_active_demo BOOLEAN;
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS is_active_demo_flag SMALLINT;
UPDATE smartbi_exchange.sb_fact_intervention_outcome
   SET is_active_demo = CASE WHEN data_origin <> 'LIVE_DEMO' THEN TRUE ELSE EXISTS (
           SELECT 1 FROM app.demo_runs d WHERE d.demo_run_id = sb_fact_intervention_outcome.demo_run_id AND d.status = 'ACTIVE'
       ) END,
       is_active_demo_flag = CASE WHEN data_origin <> 'LIVE_DEMO' THEN 1 ELSE CASE WHEN EXISTS (
           SELECT 1 FROM app.demo_runs d WHERE d.demo_run_id = sb_fact_intervention_outcome.demo_run_id AND d.status = 'ACTIVE'
       ) THEN 1 ELSE 0 END END
 WHERE is_active_demo IS NULL OR is_active_demo_flag IS NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ALTER COLUMN is_active_demo SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ALTER COLUMN is_active_demo_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD CONSTRAINT chk_sb_fact_intervention_outcome_is_active_demo_flag CHECK (is_active_demo_flag IN (0, 1));
