ALTER TABLE reservas ALTER COLUMN id_cliente TYPE VARCHAR(36) USING id_cliente::text;
