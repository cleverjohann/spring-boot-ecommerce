CREATE TABLE token_blacklist (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(1024) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL
);
