CREATE TABLE callers (
    caller_id BIGSERIAL PRIMARY KEY,
    caller_number VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE cdr_data (
    call_id BIGSERIAL PRIMARY KEY,
    call_type VARCHAR(2) NOT NULL,
    caller_number VARCHAR(20) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    transaction_status VARCHAR(20) NOT NULL,
    send_time TIMESTAMP NOT NULL
);