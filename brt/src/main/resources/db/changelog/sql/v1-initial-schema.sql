-- Таблица для хранения подписчиков
CREATE TABLE subscribers (
    subscriber_id   BIGSERIAL        PRIMARY KEY,
    subscriber_name VARCHAR(100)  NOT NULL
);

-- Таблица для хранения абонентов
CREATE TABLE callers (
    caller_id     BIGSERIAL    PRIMARY KEY,
    subscriber_id BIGSERIAL       NOT NULL
        REFERENCES subscribers(subscriber_id)
        ON DELETE RESTRICT,
    number        VARCHAR(20) NOT NULL,
    rate_id       BIGSERIAL       NOT NULL,
    rate_date     TIMESTAMP NOT NULL,
    balance       DECIMAL(10, 2) NOT NULL
);

-- Таблица для хранения звонков
CREATE TABLE calls (
    call_id        BIGSERIAL      PRIMARY KEY,
    caller_id      BIGSERIAL         NOT NULL
                               REFERENCES callers(caller_id)
                               ON DELETE CASCADE,
    call_type      VARCHAR(2) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    start_time     TIMESTAMP   NOT NULL,
    end_time       TIMESTAMP   NOT NULL
);

-- Таблица для хранения ресурсов
CREATE TABLE resources (
    resource_id BIGSERIAL PRIMARY KEY,
    resource_name VARCHAR(20) NOT NULL
);

-- Таблица для хранения ресурсов абонентов
CREATE TABLE caller_resources (
    caller_id BIGINT NOT NULL REFERENCES callers(caller_id) ON DELETE CASCADE,
    resource_id BIGINT NOT NULL REFERENCES resources(resource_id) ON DELETE CASCADE,
    current_balance DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (caller_id, resource_id)
);

-- Таблица для хранения транзакций
CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    caller_id BIGINT NOT NULL REFERENCES callers(caller_id) ON DELETE CASCADE,
    transaction_type VARCHAR(10) NOT NULL,
    resource_id BIGINT NOT NULL REFERENCES resources(resource_id) ON DELETE RESTRICT,
    resource_amount DECIMAL(10, 2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL
);

-- Таблица для хранения денежных транзакций
CREATE TABLE money_transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    caller_id BIGINT NOT NULL REFERENCES callers(caller_id) ON DELETE CASCADE,
    resource_amount DECIMAL(19,2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL
);