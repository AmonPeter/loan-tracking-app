CREATE TABLE interest_rate_settings (
    id BIGSERIAL PRIMARY KEY,
    interest_rate NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_interest_rate_settings_rate_non_negative CHECK (interest_rate >= 0)
);

CREATE INDEX idx_interest_rate_settings_created_at ON interest_rate_settings (created_at DESC, id DESC);
