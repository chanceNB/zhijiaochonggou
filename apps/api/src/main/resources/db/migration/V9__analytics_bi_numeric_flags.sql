-- Keep the canonical Boolean facts and add numeric projections for BI tools.
ALTER TABLE smartbi_exchange.sb_fact_learning_state
    ADD COLUMN IF NOT EXISTS is_current_flag SMALLINT;
UPDATE smartbi_exchange.sb_fact_learning_state
SET is_current_flag = CASE WHEN is_current THEN 1 ELSE 0 END
WHERE is_current_flag IS NULL;
ALTER TABLE smartbi_exchange.sb_fact_learning_state
    ALTER COLUMN is_current_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_learning_state
    ADD CONSTRAINT chk_sb_fact_learning_state_is_current_flag CHECK (is_current_flag IN (0, 1));

ALTER TABLE smartbi_exchange.sb_fact_practice_attempt
    ADD COLUMN IF NOT EXISTS correct_flag SMALLINT;
ALTER TABLE smartbi_exchange.sb_fact_practice_attempt
    ADD COLUMN IF NOT EXISTS is_active_demo_flag SMALLINT;
UPDATE smartbi_exchange.sb_fact_practice_attempt
SET correct_flag = CASE WHEN correct THEN 1 ELSE 0 END,
    is_active_demo_flag = CASE WHEN is_active_demo THEN 1 ELSE 0 END
WHERE correct_flag IS NULL OR is_active_demo_flag IS NULL;
ALTER TABLE smartbi_exchange.sb_fact_practice_attempt
    ALTER COLUMN correct_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_practice_attempt
    ALTER COLUMN is_active_demo_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_practice_attempt
    ADD CONSTRAINT chk_sb_fact_practice_attempt_correct_flag CHECK (correct_flag IN (0, 1));
ALTER TABLE smartbi_exchange.sb_fact_practice_attempt
    ADD CONSTRAINT chk_sb_fact_practice_attempt_is_active_demo_flag CHECK (is_active_demo_flag IN (0, 1));

ALTER TABLE smartbi_exchange.sb_fact_wrong_book
    ADD COLUMN IF NOT EXISTS is_active_demo_flag SMALLINT;
UPDATE smartbi_exchange.sb_fact_wrong_book
SET is_active_demo_flag = CASE WHEN is_active_demo THEN 1 ELSE 0 END
WHERE is_active_demo_flag IS NULL;
ALTER TABLE smartbi_exchange.sb_fact_wrong_book
    ALTER COLUMN is_active_demo_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_fact_wrong_book
    ADD CONSTRAINT chk_sb_fact_wrong_book_is_active_demo_flag CHECK (is_active_demo_flag IN (0, 1));

ALTER TABLE smartbi_exchange.sb_demo_run_state
    ADD COLUMN IF NOT EXISTS active_flag SMALLINT;
UPDATE smartbi_exchange.sb_demo_run_state
SET active_flag = CASE WHEN active THEN 1 ELSE 0 END
WHERE active_flag IS NULL;
ALTER TABLE smartbi_exchange.sb_demo_run_state
    ALTER COLUMN active_flag SET NOT NULL;
ALTER TABLE smartbi_exchange.sb_demo_run_state
    ADD CONSTRAINT chk_sb_demo_run_state_active_flag CHECK (active_flag IN (0, 1));
