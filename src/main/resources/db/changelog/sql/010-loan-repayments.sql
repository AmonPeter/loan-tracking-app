CREATE TABLE loan_repayments (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    payment_amount NUMERIC(19,2) NOT NULL,
    payment_date DATE NOT NULL,
    repayment_month INTEGER NOT NULL,
    repayment_year INTEGER NOT NULL,
    payment_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_loan_repayments_payment_amount_positive CHECK (payment_amount > 0),
    CONSTRAINT chk_loan_repayments_month_range CHECK (repayment_month >= 1 AND repayment_month <= 12),
    CONSTRAINT chk_loan_repayments_year_range CHECK (repayment_year >= 2000 AND repayment_year <= 2200)
);

CREATE INDEX idx_loan_repayments_loan_id ON loan_repayments (loan_id);
