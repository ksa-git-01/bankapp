CREATE TABLE exchange_rate (
    id BIGSERIAL PRIMARY KEY,
    currency_from VARCHAR(10) NOT NULL,
    currency_to VARCHAR(10) NOT NULL,
    ratio NUMERIC NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exchange_history (
    id BIGSERIAL PRIMARY KEY,
    user_id_from BIGINT NOT NULL,
    currency_from VARCHAR(10) NOT NULL,
    amount_from NUMERIC NOT NULL,
    user_id_to BIGINT NOT NULL,
    currency_to VARCHAR(10) NOT NULL,
    amount_to NUMERIC NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
