ALTER TABLE loans
    ADD COLUMN repayment_start_date INTEGER NULL;

ALTER TABLE loans
    ADD CONSTRAINT chk_loans_repayment_start_date_values
        CHECK (repayment_start_date IS NULL OR repayment_start_date IN (1, 15));
