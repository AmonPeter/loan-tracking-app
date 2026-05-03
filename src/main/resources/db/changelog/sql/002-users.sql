CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_users_password_hash_len CHECK (char_length(password_hash) = 60)
);

CREATE UNIQUE INDEX uq_users_email_lower ON users ((lower(email)));

INSERT INTO users (email, password_hash, role)
VALUES ('user@example.com', '$2a$12$bTy1kmuAq7hKN0q5wtM3AewnTc.OVPu475VblwSxJYOIZpyqm8Niy', 'ADMIN');
