-- Dueño de cada propiedad: el sub del JWT del vendedor (UUID de Keycloak). Las propiedades anteriores quedan sin dueño y solo las gestiona ADMIN.
ALTER TABLE propiedades ADD COLUMN id_vendedor VARCHAR(36);
