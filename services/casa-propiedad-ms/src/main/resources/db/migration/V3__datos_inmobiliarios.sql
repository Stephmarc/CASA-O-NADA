ALTER TABLE propiedades ADD COLUMN direccion VARCHAR(200) NOT NULL DEFAULT 'Por confirmar';
ALTER TABLE propiedades ADD COLUMN ciudad VARCHAR(80) NOT NULL DEFAULT 'Lima';
ALTER TABLE propiedades ADD COLUMN area_m2 INTEGER NOT NULL DEFAULT 100;
ALTER TABLE propiedades ADD COLUMN dormitorios INTEGER NOT NULL DEFAULT 2;
ALTER TABLE propiedades ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE propiedades ADD CONSTRAINT disponibilidad CHECK(stock BETWEEN 0 AND 1);
UPDATE propiedades SET direccion='Av. Los Jardines 120', area_m2=120, dormitorios=3 WHERE id=1;
UPDATE propiedades SET direccion='Calle Los Pinos 240',area_m2=80 WHERE id=2;
UPDATE propiedades SET direccion='Jr. Las Palmeras 300',ciudad='Tarapoto',area_m2=200,dormitorios=0 WHERE id=3;
