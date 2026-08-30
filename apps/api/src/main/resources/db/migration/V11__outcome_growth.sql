-- T08 owns the execution boundary after a committed teacher intervention.
-- Learning state remains A-owned; these tables store execution evidence and B-owned outcome facts.

ALTER TABLE app.intervention_assignments
    ADD COLUMN IF NOT EXISTS demo_run_id VARCHAR(128);
ALTER TABLE app.intervention_assignments
    ADD COLUMN IF NOT EXISTS demo_case_id VARCHAR(128);
ALTER TABLE app.intervention_assignments
    ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(128);
ALTER TABLE app.intervention_assignments
    ADD COLUMN IF NOT EXISTS source_version VARCHAR(64);
UPDATE app.intervention_assignments a
   SET demo_run_id = i.demo_run_id,
       demo_case_id = i.demo_case_id,
       correlation_id = i.correlation_id,
       source_version = i.source_version
  FROM app.interventions i
 WHERE i.intervention_id = a.intervention_id
   AND a.source_version IS NULL;
ALTER TABLE app.intervention_assignments
    ALTER COLUMN source_version SET NOT NULL;

ALTER TABLE app.intervention_assignments
    DROP CONSTRAINT IF EXISTS intervention_assignments_status_check;
-- H2 gives the inline V10 check a generated name; PostgreSQL simply ignores this clause.
ALTER TABLE app.intervention_assignments
    DROP CONSTRAINT IF EXISTS CONSTRAINT_9F2181BE_2;
ALTER TABLE app.intervention_assignments
    ADD CONSTRAINT intervention_assignments_status_check
        CHECK (status IN ('PENDING_STUDENT', 'IN_PROGRESS', 'COMPLETED'));

ALTER TABLE app.practice_questions
    ADD COLUMN IF NOT EXISTS validation_role VARCHAR(32);
UPDATE app.practice_questions
   SET validation_role = 'DIAGNOSTIC'
 WHERE validation_role IS NULL;
ALTER TABLE app.practice_questions
    ALTER COLUMN validation_role SET NOT NULL;
ALTER TABLE app.practice_questions
    ALTER COLUMN validation_role SET DEFAULT 'DIAGNOSTIC';
ALTER TABLE app.practice_questions
    ADD CONSTRAINT practice_questions_validation_role_check
        CHECK (validation_role IN ('DIAGNOSTIC', 'TRANSFER'));

ALTER TABLE app.practice_attempts
    ADD COLUMN IF NOT EXISTS intervention_id VARCHAR(160)
        REFERENCES app.interventions(intervention_id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_practice_attempts_intervention
    ON app.practice_attempts (intervention_id, attempt_time);

ALTER TABLE app.interventions
    ADD COLUMN IF NOT EXISTS mastery_before NUMERIC(8,6);
ALTER TABLE app.interventions
    ADD COLUMN IF NOT EXISTS confidence_before NUMERIC(8,6);
ALTER TABLE app.interventions
    ADD COLUMN IF NOT EXISTS forgetting_risk_before NUMERIC(8,6);
ALTER TABLE app.interventions
    ADD COLUMN IF NOT EXISTS weakness_score_before NUMERIC(8,6);
ALTER TABLE app.interventions
    ADD COLUMN IF NOT EXISTS evidence_count_before INTEGER;
ALTER TABLE app.interventions
    ADD COLUMN IF NOT EXISTS before_captured_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE IF NOT EXISTS app.transfer_validations (
    transfer_validation_id VARCHAR(160) PRIMARY KEY,
    intervention_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.interventions(intervention_id) ON DELETE CASCADE,
    assignment_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.intervention_assignments(assignment_id) ON DELETE CASCADE,
    practice_set_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.practice_sets(practice_set_id),
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    result VARCHAR(32) NOT NULL CHECK (result IN ('PASS', 'FAIL', 'NOT_RUN')),
    attempt_count INTEGER NOT NULL CHECK (attempt_count >= 0),
    correct_count INTEGER NOT NULL CHECK (correct_count >= 0),
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS app.intervention_outcomes (
    outcome_id VARCHAR(160) PRIMARY KEY,
    intervention_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.interventions(intervention_id) ON DELETE CASCADE,
    assignment_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.intervention_assignments(assignment_id) ON DELETE CASCADE,
    practice_set_id VARCHAR(160) NOT NULL UNIQUE REFERENCES app.practice_sets(practice_set_id) ON DELETE CASCADE,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    class_id VARCHAR(128) NOT NULL REFERENCES app.classrooms(class_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    predicted_lift NUMERIC(8,6) NOT NULL CHECK (predicted_lift >= 0 AND predicted_lift <= 1),
    prediction_low NUMERIC(8,6) NOT NULL CHECK (prediction_low >= 0 AND prediction_low <= 1),
    prediction_high NUMERIC(8,6) NOT NULL CHECK (prediction_high >= 0 AND prediction_high <= 1),
    mastery_before NUMERIC(8,6) NOT NULL CHECK (mastery_before >= 0 AND mastery_before <= 1),
    confidence_before NUMERIC(8,6) NOT NULL CHECK (confidence_before >= 0 AND confidence_before <= 1),
    forgetting_risk_before NUMERIC(8,6) NOT NULL CHECK (forgetting_risk_before >= 0 AND forgetting_risk_before <= 1),
    weakness_score_before NUMERIC(8,6),
    evidence_count_before INTEGER NOT NULL CHECK (evidence_count_before > 0),
    mastery_after NUMERIC(8,6) NOT NULL CHECK (mastery_after >= 0 AND mastery_after <= 1),
    confidence_after NUMERIC(8,6) NOT NULL CHECK (confidence_after >= 0 AND confidence_after <= 1),
    forgetting_risk_after NUMERIC(8,6) NOT NULL CHECK (forgetting_risk_after >= 0 AND forgetting_risk_after <= 1),
    evidence_count_after INTEGER NOT NULL CHECK (evidence_count_after > 0),
    actual_lift NUMERIC(8,6) NOT NULL CHECK (actual_lift >= -1 AND actual_lift <= 1),
    prediction_deviation NUMERIC(8,6) NOT NULL CHECK (prediction_deviation >= -1 AND prediction_deviation <= 1),
    transfer_validation VARCHAR(32) NOT NULL CHECK (transfer_validation IN ('PASS', 'FAIL', 'NOT_RUN')),
    practice_accuracy_after NUMERIC(8,6) NOT NULL CHECK (practice_accuracy_after >= 0 AND practice_accuracy_after <= 1),
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    data_origin VARCHAR(32) NOT NULL,
    demo_run_id VARCHAR(128),
    demo_case_id VARCHAR(128),
    correlation_id VARCHAR(128),
    source_version VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_intervention_outcomes_student
    ON app.intervention_outcomes (student_id, course_id, completed_at);

ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS assignment_id VARCHAR(160);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS practice_set_id VARCHAR(160);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS predicted_lift NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS prediction_low NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS prediction_high NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS mastery_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS confidence_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS forgetting_risk_before NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS evidence_count_before INTEGER;
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS confidence_after NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS forgetting_risk_after NUMERIC(8,6);
ALTER TABLE smartbi_exchange.sb_fact_intervention_outcome
    ADD COLUMN IF NOT EXISTS evidence_count_after INTEGER;
