-- Scoped T04 recomputations may publish derived state from the active demo run.
-- Baseline remains valid and is retained in learning_snapshot_history.
ALTER TABLE app.learning_snapshots
    DROP CONSTRAINT IF EXISTS learning_snapshots_data_origin_check;
ALTER TABLE app.learning_snapshots
    DROP CONSTRAINT IF EXISTS CONSTRAINT_FBA0832E_0;
ALTER TABLE app.learning_snapshots
    ADD CONSTRAINT learning_snapshots_data_origin_check
    CHECK (data_origin IN ('BASELINE_SIMULATED', 'LIVE_DEMO'));

ALTER TABLE app.student_learning_abilities
    DROP CONSTRAINT IF EXISTS student_learning_abilities_data_origin_check;
ALTER TABLE app.student_learning_abilities
    DROP CONSTRAINT IF EXISTS CONSTRAINT_17149;
ALTER TABLE app.student_learning_abilities
    ADD CONSTRAINT student_learning_abilities_data_origin_check
    CHECK (data_origin IN ('BASELINE_SIMULATED', 'LIVE_DEMO'));

ALTER TABLE app.weak_knowledge_point_candidates
    DROP CONSTRAINT IF EXISTS weak_knowledge_point_candidates_data_origin_check;
ALTER TABLE app.weak_knowledge_point_candidates
    DROP CONSTRAINT IF EXISTS CONSTRAINT_12EAE067_1;
ALTER TABLE app.weak_knowledge_point_candidates
    ADD CONSTRAINT weak_knowledge_point_candidates_data_origin_check
    CHECK (data_origin IN ('BASELINE_SIMULATED', 'LIVE_DEMO'));
