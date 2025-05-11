-- Вставка в таблицу rates
INSERT INTO rates (rate_id, rate_name, rate_type, period_duration, period_price, added_minutes)
VALUES
(11, 'Классический', 'CLASSIC', 0, 0.0, 0),
(12, 'Помесячный', 'MONTHLY', 30, 100.0, 50);

-- Вставка в таблицу params
INSERT INTO params (param_id, param_name)
VALUES
(1, 'минуты');

-- Вставка в таблицу call_cost
INSERT INTO call_cost (call_cost_id, rate_id, call_type, network_type, call_cost)
VALUES
(1, 11, '01', 'INTERNAL', 1.50),
(2, 11, '02', 'INTERNAL', 0.00),
(3, 11, '01', 'EXTERNAL', 2.50),
(4, 11, '02', 'EXTERNAL', 0.00);

-- Вставка в таблицу rate_params
INSERT INTO rate_params (rate_id, param_id, param_value)
VALUES
(12, 1, 50.00);
