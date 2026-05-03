ALTER TABLE loans
ADD COLUMN grace_period_days INTEGER NOT NULL DEFAULT 30;

ALTER TABLE loans
ADD CONSTRAINT chk_loans_grace_period_days CHECK (grace_period_days IN (30, 60, 90, 120, 160, 180, 210));
