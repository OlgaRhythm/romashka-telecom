-- Вставка в таблицу rates
INSERT INTO rates (rate_id, rate_name, rate_type, period_duration, period_price, added_minutes)
VALUES
(11, 'Классический', 'CLASSIC', 0, 300.00, 100),
(12, 'Помесячный', 'MONTHLY', 30, 2500.00, 500);

-- Вставка в таблицу params
INSERT INTO params (param_name)
VALUES
('минуты');

-- Вставка в таблицу call_cost (предполагаем, что calls.call_id 1 и 2 существуют)
INSERT INTO call_cost (call_cost_id, call_type, call_cost)
VALUES
(1, '01', 15.50),
(2, '02', 23.75);

-- Вставка в таблицу rate_params
INSERT INTO rate_params (rate_id, param_id, param_value)
VALUES
(11, 1, '500'),
(12, 1, '2000');
