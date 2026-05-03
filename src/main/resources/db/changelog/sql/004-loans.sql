CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    project_description TEXT NOT NULL,
    applicant_first_name VARCHAR(100) NOT NULL,
    applicant_surname VARCHAR(100) NOT NULL,
    applicant_id_number VARCHAR(50) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    region VARCHAR(100) NOT NULL,
    town_village VARCHAR(100) NOT NULL,
    membership_status VARCHAR(50) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    conditions_precedent TEXT,
    interest_rate NUMERIC(5,2) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    loan_status VARCHAR(50) NOT NULL,
    loan_status_comment TEXT,
    loan_conditions TEXT,
    approved_amount NUMERIC(19,2) NOT NULL,
    disbursed_amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_loans_interest_rate_non_negative CHECK (interest_rate >= 0),
    CONSTRAINT chk_loans_approved_amount_non_negative CHECK (approved_amount >= 0),
    CONSTRAINT chk_loans_disbursed_amount_non_negative CHECK (disbursed_amount >= 0),
    CONSTRAINT chk_loans_disbursed_lte_approved CHECK (disbursed_amount <= approved_amount)
);

CREATE INDEX idx_loans_applicant_id_number ON loans (applicant_id_number);
CREATE INDEX idx_loans_status ON loans (loan_status);
