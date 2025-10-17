INSERT INTO accounts (user_id, currency, balance)
SELECT
    u.id as user_id,
    c.currency,
    -- Генерируем случайную сумму от 1 до 100000
    FLOOR(1 + RANDOM() * 99999) as balance
FROM users u
CROSS JOIN (
    VALUES ('RUB'), ('USD'), ('EUR')
) AS c(currency)

WHERE NOT EXISTS (
    SELECT 1
    FROM accounts a
    WHERE a.user_id = u.id
    AND a.currency = c.currency
);