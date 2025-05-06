CREATE TABLE subscribers (
    subscriber_id   BIGSERIAL        PRIMARY KEY,
    subscriber_name VARCHAR(100)  NOT NULL
);

CREATE TABLE callers (
    caller_id     BIGSERIAL    PRIMARY KEY,
    subscriber_id BIGSERIAL       NOT NULL
        REFERENCES subscribers(subscriber_id)
        ON DELETE RESTRICT,
    number        VARCHAR(20) NOT NULL,
    rate_id       BIGSERIAL       NOT NULL
        REFERENCES rates(rate_id)
        ON DELETE RESTRICT,
    rate_date     TIMESTAMP NOT NULL,
    balance       DECIMAL(10, 2) NOT NULL
);

CREATE TABLE calls (
    call_id        SERIAL      PRIMARY KEY,
    caller_id      INT         NOT NULL
                               REFERENCES callers(caller_id)
                               ON DELETE CASCADE,
    call_type      VARCHAR(2) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    start_time     TIMESTAMP   NOT NULL,
    end_time       TIMESTAMP   NOT NULL
);

CREATE TABLE resourse_type (
    resourse_type_id SERIAL PRIMARY KEY,
    resourse_type_name VARCHAR(20) NOT NULL
);