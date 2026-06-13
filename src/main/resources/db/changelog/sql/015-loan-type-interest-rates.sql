ALTER TABLE loan_types
    ADD COLUMN IF NOT EXISTS interest_rate NUMERIC(5,2) NOT NULL DEFAULT 0;

ALTER TABLE loan_types
    DROP CONSTRAINT IF EXISTS chk_loan_types_interest_rate_non_negative;

ALTER TABLE loan_types
    ADD CONSTRAINT chk_loan_types_interest_rate_non_negative CHECK (interest_rate >= 0);

UPDATE loan_types
SET interest_rate = CASE name
    WHEN 'Micro loan' THEN 12.00
    WHEN 'Education Support loan' THEN 5.00
    WHEN 'SME loan' THEN 12.50
    WHEN 'Not Stated' THEN 6.50
    ELSE interest_rate
END,
updated_at = CURRENT_TIMESTAMP;

DROP TABLE IF EXISTS interest_rate_settings;
