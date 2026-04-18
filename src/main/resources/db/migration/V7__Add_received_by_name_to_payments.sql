-- Agrega la columna received_by_name a la tabla payments
ALTER TABLE payments ADD COLUMN IF NOT EXISTS received_by_name VARCHAR(120);

-- Rellena los pagos existentes con el nombre del usuario correspondiente
UPDATE payments p
JOIN users u ON u.email = p.received_by
SET p.received_by_name = u.name
WHERE p.received_by_name IS NULL AND p.received_by IS NOT NULL;
