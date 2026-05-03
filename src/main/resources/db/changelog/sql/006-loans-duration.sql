ALTER TABLE loans
ADD COLUMN duration_months INTEGER NOT NULL DEFAULT 12;

ALTER TABLE loans
ADD CONSTRAINT chk_loans_duration_months CHECK (duration_months IN (12, 36));
