ALTER TABLE loans
    ADD COLUMN repayment_start_month INTEGER NULL,
    ADD COLUMN repayment_start_year INTEGER NULL;

ALTER TABLE loans
    ADD CONSTRAINT chk_loans_repayment_start_month_range
        CHECK (repayment_start_month IS NULL OR (repayment_start_month >= 1 AND repayment_start_month <= 12));

ALTER TABLE loans
    ADD CONSTRAINT chk_loans_repayment_start_year_range
        CHECK (repayment_start_year IS NULL OR (repayment_start_year >= 2000 AND repayment_start_year <= 2200));
