-- Создание таблицы тарифов
CREATE TABLE rates (
    rate_id         BIGSERIAL PRIMARY KEY,
    rate_name       VARCHAR(100) NOT NULL,
    rate_type       VARCHAR(20) NOT NULL,
    period_duration BIGINT NOT NULL,
    period_price    DECIMAL(10, 2) NOT NULL,
    added_minutes   BIGINT NOT NULL
);

-- Создание таблицы параметров
CREATE TABLE params (
    param_id    BIGSERIAL PRIMARY KEY,
    param_name  VARCHAR(100) NOT NULL UNIQUE
);

-- Создание таблицы стоимости звонков
CREATE TABLE call_cost (
    call_cost_id BIGSERIAL PRIMARY KEY,
    call_type    VARCHAR(20) NOT NULL,
    call_cost    DECIMAL(10, 2) NOT NULL
);

-- Создание таблицы параметров тарифов (составной первичный ключ)
CREATE TABLE rate_params (
    rate_id      BIGINT NOT NULL REFERENCES rates(rate_id),
    param_id     BIGINT NOT NULL REFERENCES params(param_id),
    param_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (rate_id, param_id)
);