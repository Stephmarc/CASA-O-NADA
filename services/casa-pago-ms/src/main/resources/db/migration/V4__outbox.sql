CREATE TABLE eventos_pendientes(id BIGSERIAL PRIMARY KEY,tipo_evento VARCHAR(255),reserva_id BIGINT,monto NUMERIC(19,2), estado VARCHAR(30),origen VARCHAR(255),timestamp BIGINT);
