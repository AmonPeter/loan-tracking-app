CREATE TABLE IF NOT EXISTS loan_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_loan_types_name UNIQUE (name),
    CONSTRAINT chk_loan_types_name_not_blank CHECK (length(trim(name)) > 0)
);

INSERT INTO loan_types (name) VALUES
('Micro loan'),
('Education Support loan'),
('SME loan'),
('Not Stated')
ON CONFLICT (name) DO UPDATE
SET active = TRUE,
    updated_at = CURRENT_TIMESTAMP;

TRUNCATE TABLE loan_repayments RESTART IDENTITY CASCADE;
TRUNCATE TABLE loans RESTART IDENTITY CASCADE;

ALTER TABLE loans DROP COLUMN IF EXISTS loan_type;
ALTER TABLE loans ADD COLUMN IF NOT EXISTS loan_type_id BIGINT;
ALTER TABLE loans ALTER COLUMN loan_type_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_loans_loan_type'
    ) THEN
        ALTER TABLE loans
            ADD CONSTRAINT fk_loans_loan_type
            FOREIGN KEY (loan_type_id)
            REFERENCES loan_types (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_loans_loan_type_id ON loans (loan_type_id);

WITH seed AS (
    SELECT
        g,
        (ARRAY['Maria','Johannes','Lydia','Petrus','Selma','Thomas','Anna','Mateus','Helena','Samuel','Erastus','Monica','Victor','Ester','Tobias','Alina','Paulus','Nangula','Elifas','Rosa'])[((g - 1) % 20) + 1] AS applicant_first_name,
        (ARRAY['Nambinga','Shilongo','Kandjii','Uushona','Munuandjua','Nekundi','Nghipandulwa','Shivute','Katjiuanjo','Amutenya','Nakashole','Haikera','Shikongo','Mbenzi','Nande','Kaaronda','Nghituwamata','Iitula','Hamukwaya','Tjipangandjara'])[((g * 7 - 1) % 20) + 1] AS applicant_surname,
        (ARRAY['Khomas','Oshana','Erongo','Omusati','Omaheke','Hardap','Ohangwena','Oshikoto','Otjozondjupa','Kavango East','Zambezi','Kavango West','Kunene','Karas'])[((g * 5 - 1) % 14) + 1] AS region,
        (ARRAY['Windhoek','Ondangwa','Walvis Bay','Outapi','Gobabis','Mariental','Eenhana','Tsumeb','Otjiwarongo','Rundu','Katima Mulilo','Nkurenkuru','Opuwo','Keetmanshoop'])[((g * 5 - 1) % 14) + 1] AS town_village,
        (ARRAY['Micro loan','Education Support loan','SME loan','Not Stated'])[((g * 11 - 1) % 4) + 1] AS loan_type_name,
        (ARRAY['Member','Dependent','Registered Ex Member'])[((g * 13 - 1) % 3) + 1] AS membership_status,
        (ARRAY['Male','Female'])[((g * 17 - 1) % 2) + 1] AS gender,
        (ARRAY[30,60,90,120,160,180,210])[((g * 19 - 1) % 7) + 1] AS grace_period_days,
        CASE
            WHEN g % 20 IN (0, 1) THEN 'Declined'
            WHEN g % 20 IN (2, 3) THEN 'Withdrawn'
            WHEN g % 20 IN (4, 5) THEN 'Initiation'
            ELSE 'Approved'
        END AS loan_status
    FROM generate_series(1, 1000) AS g
),
loan_data AS (
    SELECT
        seed.g,
        'Generated testing loan #' || seed.g AS project_description,
        seed.applicant_first_name,
        seed.applicant_surname,
        'TEST' || lpad((1000000 + seed.g)::text, 7, '0') AS applicant_id_number,
        '+26481' || lpad((2000000 + seed.g)::text, 7, '0') AS contact_number,
        seed.region,
        seed.town_village,
        seed.membership_status,
        seed.gender,
        CASE
            WHEN seed.g % 6 = 0 THEN 'Supplier quote pending'
            WHEN seed.g % 6 = 1 THEN 'Income verification complete'
            WHEN seed.g % 6 = 2 THEN 'Committee approval required'
            ELSE NULL
        END AS conditions_precedent,
        CASE
            WHEN seed.loan_type_name = 'Education Support loan' THEN (4.00 + ((seed.g * 23) % 325) / 100.0)::numeric(5,2)
            WHEN seed.loan_type_name = 'SME loan' THEN (9.50 + ((seed.g * 29) % 700) / 100.0)::numeric(5,2)
            WHEN seed.loan_type_name = 'Micro loan' THEN (8.00 + ((seed.g * 31) % 850) / 100.0)::numeric(5,2)
            ELSE (6.50 + ((seed.g * 37) % 600) / 100.0)::numeric(5,2)
        END AS interest_rate,
        loan_types.id AS loan_type_id,
        CASE WHEN seed.g % 3 = 0 THEN 36 ELSE 12 END AS duration_months,
        seed.grace_period_days,
        seed.loan_status,
        CASE seed.loan_status
            WHEN 'Approved' THEN 'Generated test approval with varied repayment behavior'
            WHEN 'Declined' THEN 'Generated test decline scenario'
            WHEN 'Withdrawn' THEN 'Generated test withdrawal scenario'
            ELSE 'Generated test application in progress'
        END AS loan_status_comment,
        CASE
            WHEN seed.loan_status = 'Approved' AND seed.g % 4 = 0 THEN 'Monthly repayment monitoring required'
            WHEN seed.loan_status = 'Approved' AND seed.g % 4 = 1 THEN 'Quarterly business update required'
            ELSE NULL
        END AS loan_conditions,
        (5000 + ((seed.g * 7919) % 2450) * 100)::numeric(19,2) AS approved_amount,
        CASE
            WHEN seed.loan_status = 'Approved' THEN round(((5000 + ((seed.g * 7919) % 2450) * 100) * (60 + ((seed.g * 43) % 41)) / 100.0)::numeric, 2)
            ELSE 0::numeric(19,2)
        END AS disbursed_amount,
        CASE
            WHEN seed.loan_status = 'Approved' THEN current_date - ((seed.g * 13) % 1460)
            ELSE NULL
        END AS disbursement_date
    FROM seed
    JOIN loan_types ON loan_types.name = seed.loan_type_name
),
scheduled_loan_data AS (
    SELECT
        loan_data.*,
        CASE WHEN loan_status = 'Approved' THEN (disbursement_date + INTERVAL '1 month')::date ELSE NULL END AS repayment_start
    FROM loan_data
)
INSERT INTO loans (
    project_description,
    applicant_first_name,
    applicant_surname,
    applicant_id_number,
    contact_number,
    region,
    town_village,
    membership_status,
    gender,
    conditions_precedent,
    interest_rate,
    loan_type_id,
    duration_months,
    grace_period_days,
    loan_status,
    loan_status_comment,
    loan_conditions,
    approved_amount,
    disbursed_amount,
    disbursement_date,
    repayment_start_month,
    repayment_start_year,
    repayment_start_date,
    created_at,
    updated_at
)
SELECT
    project_description,
    applicant_first_name,
    applicant_surname,
    applicant_id_number,
    contact_number,
    region,
    town_village,
    membership_status,
    gender,
    conditions_precedent,
    interest_rate,
    loan_type_id,
    duration_months,
    grace_period_days,
    loan_status,
    loan_status_comment,
    loan_conditions,
    approved_amount,
    disbursed_amount,
    disbursement_date,
    CASE WHEN repayment_start IS NULL THEN NULL ELSE EXTRACT(MONTH FROM repayment_start)::integer END,
    CASE WHEN repayment_start IS NULL THEN NULL ELSE EXTRACT(YEAR FROM repayment_start)::integer END,
    CASE WHEN g % 2 = 0 THEN 1 ELSE 15 END,
    COALESCE(disbursement_date, current_date - ((g * 13) % 365))::timestamp,
    CURRENT_TIMESTAMP
FROM scheduled_loan_data;

WITH test_loans AS (
    SELECT
        id AS loan_id,
        (substring(applicant_id_number FROM 5)::integer - 1000000) AS g,
        disbursed_amount,
        interest_rate,
        duration_months,
        grace_period_days,
        repayment_start_month,
        repayment_start_year,
        repayment_start_date
    FROM loans
    WHERE applicant_id_number LIKE 'TEST%'
      AND loan_status = 'Approved'
      AND disbursed_amount > 0
      AND repayment_start_month IS NOT NULL
      AND repayment_start_year IS NOT NULL
      AND repayment_start_date IS NOT NULL
),
repayment_plan AS (
    SELECT
        loan_id,
        g,
        duration_months,
        make_date(repayment_start_year, repayment_start_month, repayment_start_date) + grace_period_days AS first_due_date,
        round((
            disbursed_amount * power(1 + (interest_rate::double precision / 1200), duration_months + (grace_period_days / 30.0))
        )::numeric / duration_months, 2) AS monthly_installment
    FROM test_loans
),
due_plan AS (
    SELECT
        *,
        CASE
            WHEN current_date < first_due_date THEN 0
            ELSE LEAST(
                duration_months,
                GREATEST(
                    0,
                    ((EXTRACT(YEAR FROM age(current_date, first_due_date))::integer * 12)
                        + EXTRACT(MONTH FROM age(current_date, first_due_date))::integer
                        + CASE WHEN EXTRACT(DAY FROM current_date)::integer >= EXTRACT(DAY FROM first_due_date)::integer THEN 1 ELSE 0 END)
                )
            )
        END AS due_installments
    FROM repayment_plan
),
coverage_plan AS (
    SELECT
        *,
        CASE g % 12
            WHEN 0 THEN due_installments
            WHEN 1 THEN due_installments
            WHEN 2 THEN GREATEST(due_installments - 1, 0)
            WHEN 3 THEN GREATEST(due_installments - 2, 0)
            WHEN 4 THEN GREATEST(due_installments - 4, 0)
            WHEN 5 THEN 0
            WHEN 6 THEN floor(due_installments * 0.50)::integer
            WHEN 7 THEN due_installments
            WHEN 8 THEN GREATEST(due_installments - 3, 0)
            WHEN 9 THEN due_installments
            WHEN 10 THEN GREATEST(due_installments - 6, 0)
            ELSE GREATEST(due_installments - 1, 0)
        END AS covered_installments
    FROM due_plan
)
INSERT INTO loan_repayments (
    loan_id,
    payment_amount,
    payment_date,
    repayment_month,
    repayment_year,
    payment_note,
    created_at
)
SELECT
    coverage_plan.loan_id,
    coverage_plan.monthly_installment,
    LEAST(
        current_date,
        (coverage_plan.first_due_date + ((payment_number - 1) * INTERVAL '1 month') + (((coverage_plan.g * payment_number) % 10) * INTERVAL '1 day'))::date
    ),
    EXTRACT(MONTH FROM (coverage_plan.first_due_date + ((payment_number - 1) * INTERVAL '1 month')))::integer,
    EXTRACT(YEAR FROM (coverage_plan.first_due_date + ((payment_number - 1) * INTERVAL '1 month')))::integer,
    'Generated test repayment ' || payment_number,
    CURRENT_TIMESTAMP
FROM coverage_plan
CROSS JOIN LATERAL generate_series(1, coverage_plan.covered_installments) AS payment_number
WHERE coverage_plan.covered_installments > 0
  AND coverage_plan.monthly_installment > 0;
