CREATE TABLE loan_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    interest_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_loan_types_name UNIQUE (name),
    CONSTRAINT chk_loan_types_interest_rate_non_negative CHECK (interest_rate >= 0),
    CONSTRAINT chk_loan_types_name_not_blank CHECK (length(trim(name)) > 0)
);

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
    loan_type_id BIGINT NOT NULL,
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
    CONSTRAINT chk_loans_disbursed_lte_approved CHECK (disbursed_amount <= approved_amount),
    CONSTRAINT fk_loans_loan_type FOREIGN KEY (loan_type_id) REFERENCES loan_types (id)
);

CREATE INDEX idx_loans_applicant_id_number ON loans (applicant_id_number);
CREATE INDEX idx_loans_status ON loans (loan_status);
CREATE INDEX idx_loans_loan_type_id ON loans (loan_type_id);
