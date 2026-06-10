ALTER TABLE loans
ADD COLUMN disbursement_date DATE;

UPDATE loans
SET disbursement_date = created_at::date
WHERE disbursed_amount > 0
  AND disbursement_date IS NULL;

ALTER TABLE loans
ADD CONSTRAINT chk_loans_disbursement_date_required
CHECK (disbursed_amount = 0 OR disbursement_date IS NOT NULL);

CREATE INDEX idx_loans_disbursement_date ON loans (disbursement_date);
